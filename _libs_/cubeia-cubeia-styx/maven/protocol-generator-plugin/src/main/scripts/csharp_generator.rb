#
# CSharp output writer.
#

CS_TYPES = {
  'uint8'  => 'byte',
  'int8'   => 'byte',
  'uint16' => 'ushort',
  'int16'  => 'short',
  'int32'  => 'int',
  'uint32' => 'uint',
  'int64'  => 'long',
  'uint64' => 'ulong',
  'string' => 'string',
  'bool'   => 'bool'
}

# Member type definitions, special handling for unsigned data types,

def cstype(generic_type)
  CS_TYPES[generic_type] || camelcasetype(generic_type)
end

def capitalize_first(s) 
  s[0..0].upcase + s[1..-1]  
end

def builtin?(generic_type)
  CS_TYPES.has_key?(generic_type)
end

def enum?(type)
  $enums.has_key?(type)
end

def camelcase(str)
  result = ''
  str.split('_').each_with_index do |e,index|
    e.capitalize! if index != 0
    result << e
  end
  result
end

def camelcasetype(str)
  str.split('_').map{|e| e.capitalize}.join
end

class CSharpOut
  def initialize(output_file)
    @indent_level = 0
    @out = output_file
  end
  
  def order
    [:start_class, :nested_types, :members, :default_constructor, :field_constructor, :save, :load, :sysout, :end_class]
  end
  
  def start_class(name, class_id)
    @classname = name
    static_tag = @indent_level != 0 ? ' static' : ''
    parent_tag = @indent_level == 0 ? ' : ProtocolObject' : ''
  
    wi("public#{static_tag} class #{camelcasetype(name)}#{parent_tag} {")
    @indent_level += 1
    if !class_id.to_s.empty?
      wi("public const int CLASSID = #{class_id};")
      wi("")
      wi("public byte classId() {")
      wi("return CLASSID;",1)
      wi("}")
      wi("")
    end
  end
  
  def end_scope
    @indent_level -= 1
    wi("}")
  end
  
  alias :end_class :end_scope
  alias :end_save :end_scope
  alias :end_load :end_scope
  
  def member_scalar(scalar)
    type = cstype(scalar.obj_type)
    wi("public #{type} #{camelcase(scalar.name)};")
    return type
  end
  
  def member_list(list)
    type = cstype(list.obj_type)
    name = camelcase(list.name)
    if (list.name == "params")
      list.name = "parameters"
    end
    if (name == "params")
      name = "parameters"
    end
    # uint8 array *is* a byte-array
    if(list.obj_type == "uint8")
      wi("public byte[] #{name} = new byte[0];")
      result = "byte[]"   
    elsif(builtin?(list.obj_type))
      # If primitive, then use array instead of List  	
      wi("public #{type}[] #{name} = new #{type}[0];")
      result = "#{type}[]"
    else 
      # If complex type, then use List 
      wi("public List<#{type}> #{name} = new List<#{type}>();")
      result = "List<#{type}>"
    end
    return result
  end
  
  def member_enum(enum)
    enumtype = enum.type
    enumtype = enumtype.capitalize
    enumtype= camelcase(enumtype)
    name = camelcase(enum.name)
    wi("public Enums.#{enumtype} #{name} = Enums.make#{enumtype}(0);")
    return "Enums.#{enumtype}"
  end
  
  def start_save(struct)
    wi("")
    wi("public void save(PacketOutputStream ps) {")
    @indent_level += 1
  end
  
  def save_scalar(scalar)
    name = camelcase(scalar.name)
    if(builtin?(scalar.obj_type))
      wi("ps.save#{capitalize_first(cstype(scalar.obj_type))}(#{name});")
    else
      wi("#{name}.save(ps);")
    end
  end
  
  def save_list(list)
    name = camelcase(list.name)
    
    if(builtin?(list.obj_type))
      # Save as array
      wi("ps.saveInt(#{name}.Length);")
      wi("ps.saveArray(#{name});")
      
    else
      type = cstype(list.obj_type)
      # Save as List of Objects
      wi("if (#{name} == null) {")
      @indent_level += 1
      wi("ps.saveInt(0);")
      @indent_level -= 1
      wi("} else {")
      @indent_level += 1
      wi("#{type}[] #{camelcase(list.obj_type)}Array = #{name}.ToArray();")
      wi("ps.saveInt(#{camelcase(list.obj_type)}Array.Length);")
      wi("foreach(#{type} #{camelcase(list.obj_type)}Object in #{camelcase(list.obj_type)}Array) {")
      if(builtin?(list.obj_type))
        wi("ps.save#{cstype(list.obj_type)}(#{camelcase(list.obj_type)});",1)
      else
        wi("#{camelcase(list.obj_type)}Object.save(ps);",1)
        wi("}")
      end
      @indent_level -= 1
      wi("}")
    end
  end
  
  def start_load(struct)
    wi("")
    wi("public void load(PacketInputStream ps) {")
    @indent_level += 1
  end
  
  def load_enum(enum)
    name = enum.type.capitalize
    name = camelcase(name)
    wi("#{camelcase(enum.name)} = Enums.make#{name}(ps.loadUnsignedByte());")
  end
  
  def save_enum(enum)
    name = camelcase(enum.name)
    wi("ps.saveUnsignedByte((byte)#{name});")
  end
  
  def load_scalar(scalar)
    name = camelcase(scalar.name)
    if(builtin?(scalar.obj_type))
      wi("#{name} = ps.load#{capitalize_first(cstype(scalar.obj_type))}();")
    else
      wi("#{name} = new #{cstype(scalar.obj_type)}();")
      wi("#{name}.load(ps);")
    end
  end
  
  def load_list(list)
    name = camelcase(list.name)
    ltype = cstype(list.obj_type)
    
    
    # Array of uint8 *is* byte
    if(list.obj_type == "uint8")
      wi("int #{name}Count = ps.loadInt();")
      wi("#{name} = new byte[#{name}Count];")
      wi("if ( #{name}Count > 0 ) {")
      wi("ps.loadByteArray(#{name});",1)
      wi("}");
      
    elsif(builtin?(list.obj_type))
      wi("int #{name}Count = ps.loadInt();")
      wi("#{name} = new #{ltype}[#{name}Count];")
      wi("ps.load#{ltype.capitalize}Array(#{name});")
      
    else
      
      wi("int #{name}Count = ps.loadInt();")
      wi("#{name} = new List<#{ltype}>(#{name}Count);")
      wi("for(int i = 0; i != #{name}Count; ++i) {")
      if(builtin?(list.obj_type))
        wi("#{name}.add(ps.load#{ltype}());",1)
      else
        wi("#{ltype} _tmp = new #{ltype}();",1)
        wi("_tmp.load(ps);",1)
        wi("#{name}.Add(_tmp);",1)
      end
      wi("}")
    end
  end
  
  def object_factory(structs, version)
    wi("public class ProtocolObjectFactory : ObjectFactory {")
    wi("public int version() {",1)
    wi("return #{version};",2)
    wi("}",1)
    wi("")
    wi("public ProtocolObject create(int classId) {",1)
    wi("switch(classId) {",2)
    
    structs.each do |struct|
      wi("case #{struct.class_id}:",3)
      wi("return new #{camelcasetype(struct.name)}();",4)
    end
    
    wi("}",2)
    wi("throw new ArgumentOutOfRangeException(\"Unknown class id: \" + classId);",2)
    wi("}",1)
    wi("}")
  end
  
  
  def wi(line,extra = 0)
    if(!line.empty?)
      @out.write("    " * (@indent_level + extra))
    end
    @out.write(line + "\n")
  end
  
  def CSharpOut.gen_enum_deserializer(name, values)
    name = name.capitalize
    name = camelcase(name)
    str = "    public static #{name} make#{name}(int value) {\n"
    str<< "        switch(value) {\n"
    values.each_with_index {|v,i| str << "            case #{i}: return #{name}.#{v};\n" }
    str<< "            default: throw new ArgumentOutOfRangeException(\"Invalid enum value for #{name}: \" + value);\n"
    str<< "        }\n"
    str<< "    }\n\n"
    str
  end
  
  def CSharpOut.gen_enum(name, values)
    name = name.capitalize
    name = camelcase(name)
    "    public enum #{name} { #{values.join(', ')} };\n\n"
  end
  
  def CSharpOut.generate_enums(f, enums)
    f.write("public class Enums {\n")
    f.write("    private Enums() {}\n\n")
    enums.each_pair do |enum, values|
      f.write(gen_enum(enum.capitalize, values))
      f.write(gen_enum_deserializer(enum.capitalize, values))
    end
    f.write("}\n")
  end
  
  def sysout(list)
    wi("\n")
    wi("override public String ToString() {")
    wi("StringBuilder result = new StringBuilder(\"#{camelcasetype(@classname)} :\");", 1)
    list.each do |member| 
      wi("result.Append(\" #{member.name}[\"+#{camelcase(member.name)}+\"]\");", 1)
    end
    wi("return result.ToString();", 1)
    wi("}\n")
  end
  
  def create_field_list(members)
    result = ""
    members.each do |member|
      result << "#{member.class_definition} #{camelcase(member.name)}, "
    end
    return result.chomp(", ")
  end
  
  def field_constructor(members)
    if (members.empty?)
      # Don't create a field construct if there are no fields.
      return
    end
    
    field_list = create_field_list(members)
    wi("")
    wi("public #{camelcasetype(@classname)}(#{field_list}) {")
    members.each do |member|
      var = camelcase(member.name)
      wi("this.#{var} = #{var};", 1)      
    end
    wi("}")
  end
  
  def default_constructor()
    wi("")
    wi("/**")
    wi(" * Default constructor.")
    wi(" *")
    wi(" */")
    wi("public #{camelcasetype(@classname)}() {")
    wi("// Nothing here", 1)
    wi("}")
  end  
  
  def CSharpOut.file_header(package)
    <<HEADER_END
// I AM AUTO-GENERATED, DON'T CHECK ME INTO SUBVERSION (or else...)

using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.IO;
using System.Net;
using Styx;

namespace #{package}
{

HEADER_END
  end
end
