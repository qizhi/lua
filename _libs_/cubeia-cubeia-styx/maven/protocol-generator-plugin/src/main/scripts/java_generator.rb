#
# Java output writer.
#

JAVA_TYPES = {
  'uint8'  => 'UnsignedByte',
  'int8'   => 'byte',
  'uint16' => 'UnsignedShort',
  'int16'  => 'short',
  'int32'  => 'int',
  'uint32' => 'UnsignedInt',
  'int64'  => 'long',
  'uint64' => 'long',
  'string' => 'String',
  'bool'   => 'boolean'
}

# Member type definitions, special handling for unsigned data types,
# since java decides to suck
UNSIGNED_TYPES = {
  'UnsignedByte'  => 'int',
  'UnsignedShort' => 'int',
  'UnsignedInt'   => 'long'
}

def jtype(generic_type)
  JAVA_TYPES[generic_type] || camelcasetype(generic_type)
end

def capitalize_first(s) 
  s[0..0].upcase + s[1..-1]  
end

def builtin?(generic_type)
  JAVA_TYPES.has_key?(generic_type)
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

class JavaOut
  def initialize(output_file, visitor = false)
    @indent_level = 0
    @out = output_file
    @visitor = visitor
  end
  
  def order
    [:start_class, :nested_types, :members, :default_constructor, :field_constructor, :save, :load, :sysout, :end_class]
  end
  
  def start_class(name, class_id)
    @classname = name
    static_tag = @indent_level != 0 ? ' static' : ''
    parent_tag = @indent_level == 0 ? ' implements ProtocolObject' : ''
    if (@visitor == true and @indent_level == 0)
      visitable_tag = ', Visitable'
    end
    
    wi("@SuppressWarnings(\"unused\")")
    wi("public#{static_tag} class #{camelcasetype(name)}#{parent_tag}#{visitable_tag} {")
    @indent_level += 1
    if !class_id.to_s.empty?
      wi("public int classId() {")
      wi("return #{class_id};",1)
      wi("}")
      wi("")
      visitable()
    end
  end
  
  def visitable 
    if (@visitor == true)      
      wi("public void accept(ProtocolObjectVisitor visitor) {")
      wi("if (visitor instanceof PacketVisitor) {",1)
      wi("PacketVisitor handler = (PacketVisitor) visitor;",2)
      wi("handler.visit(this);",2)
      wi("}",1)
      wi("}")
      
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
    type = jtype(scalar.obj_type)
    if(UNSIGNED_TYPES.has_key?(type))
      type = UNSIGNED_TYPES[type]
    end
    wi("public #{type} #{camelcase(scalar.name)};")
    return type
  end
  
  def member_list(list)
    type = jtype(list.obj_type)
    # uint8 array *is* a byte-array
    if(list.obj_type == "uint8")
      wi("public byte[] #{camelcase(list.name)} = new byte[0];")
      result = "byte[]"   
    elsif(builtin?(list.obj_type))
      # If complex type, then use List 
      if (UNSIGNED_TYPES.has_key?(type))
        type = UNSIGNED_TYPES[type]
      end
    
      # If primitive, then use array instead of List  	
      wi("public #{type}[] #{camelcase(list.name)} = new #{type}[0];")
      result = "#{type}[]"
    else 
      # If complex type, then use List 
      if (UNSIGNED_TYPES.has_key?(type))
        type = UNSIGNED_TYPES[type]
      end
      wi("public List<#{type}> #{camelcase(list.name)};")
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
    wi("public void save(PacketOutputStream ps) throws IOException {")
    @indent_level += 1
  end
  
  def save_scalar(scalar)
    name = camelcase(scalar.name)
    if(builtin?(scalar.obj_type))
      wi("ps.save#{capitalize_first(jtype(scalar.obj_type))}(#{name});")
    else
      wi("#{name}.save(ps);")
    end
  end
  
  def save_list(list)
    name = camelcase(list.name)
    ltype = jtype(list.obj_type)
    
    
    if enum?(list.obj_type)
      wi("if (#{name} == null) {")
      @indent_level += 1
      wi("ps.saveInt(0);")
      @indent_level -= 1
      wi("} else {")
      @indent_level += 1
      wi("ps.saveInt(#{name}.size());")
      wi("for(int i = 0; i != #{name}.size(); ++i) {")
      wi("ps.saveUnsignedByte(#{name}.get(i).ordinal());",1)
      wi("}")
      @indent_level -= 1
      wi("}")

    elsif list.obj_type == "uint16"
      wi("ps.saveInt(#{name}.length);")
      wi("ps.saveUint16Array(#{name});")
   
    elsif list.obj_type == "uint32"
      wi("ps.saveInt(#{name}.length);")
      wi("ps.saveUint32Array(#{name});")
       
    elsif(builtin?(list.obj_type))
      # Save as array
      wi("ps.saveInt(#{name}.length);")
      wi("ps.saveArray(#{name});")
      
    else
      # Save as List of Objects
      wi("if (#{name} == null) {")
      @indent_level += 1
      wi("ps.saveInt(0);")
      @indent_level -= 1
      wi("} else {")
      @indent_level += 1
      wi("ps.saveInt(#{name}.size());")
      wi("for(int i = 0; i != #{name}.size(); ++i)")
      if(builtin?(list.obj_type))
        wi("ps.save#{jtype(list.obj_type)}(#{name}.get(i));",1)
      else
        wi("#{name}.get(i).save(ps);",1)
      end
      @indent_level -= 1
      wi("}")
    end
  end
  
  def start_load(struct)
    wi("")
    wi("public void load(PacketInputStream ps) throws IOException {")
    @indent_level += 1
  end
  
  def load_enum(enum)
    name = enum.type.capitalize
    name = camelcase(name)
    wi("if (ps.peek() == -1) {");
    @indent_level += 1
    wi("this.#{camelcase(enum.name)} = null;")
    @indent_level -= 1
    wi("} else {")
    @indent_level += 1
    wi("this.#{camelcase(enum.name)} = Enums.make#{name}(ps.loadUnsignedByte());")
    @indent_level -= 1
    wi("}")
  end
  
  def save_enum(enum)
    name = camelcase(enum.name)
    wi("if (this.#{name} == null) {");
    @indent_level += 1
    wi("ps.saveUnsignedByte(-1);")
    @indent_level -= 1
    wi("} else {")
    @indent_level += 1
    wi("ps.saveUnsignedByte(this.#{name}.ordinal());")
    @indent_level -= 1
    wi("}")
  end
  
  def load_scalar(scalar)
    name = camelcase(scalar.name)
    if(builtin?(scalar.obj_type))
      wi("#{name} = ps.load#{capitalize_first(jtype(scalar.obj_type))}();")
    else
      wi("#{name} = new #{jtype(scalar.obj_type)}();")
      wi("#{name}.load(ps);")
    end
  end
  
  def load_list(list)
    name = camelcase(list.name)
    ltype = jtype(list.obj_type)
    
    
    if(UNSIGNED_TYPES.has_key?(ltype))
      ltype = UNSIGNED_TYPES[ltype]
    end
    
    if(list.obj_type == "uint8")
      # Array of uint8 *is* byte
      wi("int #{name}Count = ps.loadInt();")
      wi("#{name} = new byte[#{name}Count];")
      wi("ps.loadByteArray(#{name});")
   
    elsif(list.obj_type == "uint16")
        wi("int #{name}Count = ps.loadInt();")
        wi("#{name} = new #{ltype}[#{name}Count];")
        wi("ps.loadUint16Array(#{name});")
    
    elsif(list.obj_type == "uint32")
        wi("int #{name}Count = ps.loadInt();")
        wi("#{name} = new #{ltype}[#{name}Count];")
        wi("ps.loadUint32Array(#{name});")
      
    elsif(builtin?(list.obj_type))
      wi("int #{name}Count = ps.loadInt();")
      wi("#{name} = new #{ltype}[#{name}Count];")
      wi("ps.load#{ltype.capitalize}Array(#{name});")
      
    else
      
      wi("int #{name}Count = ps.loadInt();")
      wi("#{name} = new ArrayList<#{ltype}>(#{name}Count);")
      wi("for(int i = 0; i != #{name}Count; ++i) {")
      if(builtin?(list.obj_type))
        wi("#{name}.add(ps.load#{ltype}());",1)
      elsif(enum?(list.obj_type))
        wi("int ordinal = ps.loadUnsignedByte();",1)
        wi("#{ltype} _tmp = Enums.make#{ltype}(ordinal);",1)
        wi("#{name}.add(_tmp);",1)
      else
        wi("#{ltype} _tmp = new #{ltype}();",1)
        wi("_tmp.load(ps);",1)
        wi("#{name}.add(_tmp);",1)
      end
      wi("}")
    end
  end
  
  def object_factory(structs, version)
  	wi("@SuppressWarnings(\"unused\")")
    wi("public class ProtocolObjectFactory implements com.cubeia.firebase.io.ObjectFactory {")
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
    wi("throw new IllegalArgumentException(\"Unknown class id: \" + classId);",2)
    wi("}",1)
    wi("}")
  end
  
  # Create Visitor interface
  def object_visitor(structs, version)
  	wi("@SuppressWarnings(\"unused\")")
    wi("public interface PacketVisitor extends ProtocolObjectVisitor {")
    structs.each do |struct|
      wi("public void visit(#{camelcasetype(struct.name)} packet);",1)
    end
    wi("}")
  end
  
  # Create Visitable interface (used by all ProtocolObjects if applicable)
  def object_visitable(structs, version)
    wi("public interface Visitable {")
    wi("public void accept(ProtocolObjectVisitor visitor);",1)
    wi("}")
  end
  
  def wi(line,extra = 0)
    if(!line.empty?)
      @out.write("    " * (@indent_level + extra))
    end
    @out.write(line + "\n")
  end
  
  def JavaOut.gen_enum_deserializer(name, values)
    name = name.capitalize
    name = camelcase(name)
    str = "    public static #{name} make#{name}(int value) {\n"
    str<< "        switch(value) {\n"
    values.each_with_index {|v,i| str << "            case #{i}: return #{name}.#{v};\n" }
    str<< "            default: throw new IllegalArgumentException(\"Invalid enum value for #{name}: \" + value);\n"
    str<< "        }\n"
    str<< "    }\n\n"
    str
  end
  
  def JavaOut.gen_enum(name, values)
    name = name.capitalize
    name = camelcase(name)
    "    public enum #{name} { #{values.join(', ')} };\n\n"
  end
  
  def JavaOut.generate_enums(f, enums)
  	f.write("@SuppressWarnings(\"unused\")")
    f.write("public final class Enums {\n")
    f.write("    private Enums() {}\n\n")
    enums.each_pair do |enum, values|
      f.write(gen_enum(enum.capitalize, values))
      f.write(gen_enum_deserializer(enum.capitalize, values))
    end
    f.write("}\n")
  end
  
  def sysout(list)
    wi("\n")
    wi("@Override\n")
    wi("public String toString() {")
    wi("final StringBuilder result = new StringBuilder(\"#{camelcasetype(@classname)} :\");", 1)
    list.each do |member| 
      if (member.class_definition == "byte[]")
        wi("result.append(\" #{member.name}[\" + ArrayUtils.toString(this.#{camelcase(member.name)}, 20)).append(\"]\");", 1)
      else
        wi("result.append(\" #{member.name}[\" + this.#{camelcase(member.name)}).append(\"]\");", 1)
      end
    end
    wi("return result.toString();", 1)
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
  
  def JavaOut.file_header(package)
    <<HEADER_END
// I AM AUTO-GENERATED, DON'T CHECK ME INTO SUBVERSION (or else...)
package #{package};

import com.cubeia.firebase.io.PacketInputStream;
import com.cubeia.firebase.io.PacketOutputStream;
import com.cubeia.firebase.io.ProtocolObject;
import com.cubeia.firebase.io.ProtocolObjectVisitor;
import com.cubeia.firebase.io.Visitable;
import com.cubeia.firebase.styx.util.ArrayUtils;

import java.util.List;
import java.util.ArrayList;
import java.io.IOException;

import #{package}.Enums.*;



HEADER_END
  end
end
