#
# Flash output writer
#

FLASH_TYPES = {
  'uint8'  => 'uint',
  'int8'   => 'int',
  'uint16' => 'uint',
  'int16'  => 'int',
  'int32'  => 'int',
  'uint32' => 'uint',
  'int64'  => 'int',
  'uint64' => 'uint',
  'string' => 'String',
  'bool'   => 'Boolean'
}

FLASH_TYPES2 = {
  'uint8'  => 'UnsignedByte',
  'int8'   => 'Byte',
  'uint16' => 'UnsignedShort',
  'int16'  => 'Short',
  'int32'  => 'Int',
  'uint32' => 'UnsignedInt',
  'int64'  => 'Long',
  'uint64' => 'Long',
  'string' => 'String',
  'bool'   => 'Boolean'
}


def ftype(generic_type)
  FLASH_TYPES[generic_type] || camelcasetype(generic_type)
end

def ftype2(generic_type)
  FLASH_TYPES2[generic_type] || camelcasetype(generic_type)
end

def fbuiltin?(generic_type)
  FLASH_TYPES.has_key?(generic_type)
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

class FlashOut
  
  
  def initialize(output_file)
    @indent_level = 0
    @out = output_file
    @index = 0 
  end

  def order
    [:start_class, :nested_types, :members, :default_constructor, :save, :load, :sysout, :end_class]
  end

  def start_class(name, class_id)
    @classname = name
    static_tag = @indent_level != 0 ? ' static' : ''
    parent_tag = @indent_level == 0 ? ' implements ProtocolObject' : ''
    @indent_level += 1
    wi("public#{static_tag} class #{camelcasetype(name)}#{parent_tag} {")
    @indent_level += 1
    if !class_id.to_s.empty?
      wi("public static const CLASSID:int = #{class_id};\n")
      wi("public function classId():int {")
      wi("return #{camelcasetype(name)}.CLASSID;",1)
      wi("}")
      wi("")
      
    end
  end
  
  def default_constructor()
    wi("")
    wi("/**")
    wi(" * Default constructor.")
    wi(" *")
    wi(" */")
    wi("public function #{camelcasetype(@classname)}() {")
    wi("// Nothing here", 1)
    wi("}")
  end  
    
  def end_scope
    @indent_level -= 1
    @index = 0
    wi("}")
  end

def end_save
     wi("return buffer;")
     @indent_level -= 1
     wi("}")
  end

  alias :end_class :end_scope
  
  alias :end_load :end_scope

  def member_scalar(scalar)
    type = ftype(scalar.obj_type)
    if(UNSIGNED_TYPES.has_key?(type))
      type = UNSIGNED_TYPES[type]
    end
    wi("public var #{camelcase(scalar.name)}:#{type};")
  end

  def member_list(list)
  	type = ftype(list.obj_type)
  	
  	# uint8 array *is* a byte-array
    if(list.obj_type == "uint8" || list.obj_type == "int8")
  	  wi("public var #{camelcase(list.name)}:ByteArray = new ByteArray();")
  	  
    elsif(fbuiltin?(list.obj_type))
      # If primitive, then use array instead of List  	
  	  wi("public var #{camelcase(list.name)}:Array = new Array();")
  	   
    else 
      # If complex type, then use List 
      if (UNSIGNED_TYPES.has_key?(type))
        type = UNSIGNED_TYPES[type]
        
      end
      wi("public var #{camelcase(list.name)}:Array = new Array();")
      
    end
  end

  def member_enum(enum)
    enumtype = enum.type
    enumtype = enumtype.capitalize
    enumtype= camelcase(enumtype)
    name = camelcase(enum.name)
    wi("public var #{name}:uint;")
  end

  def start_save(struct)
    wi("")
    wi("public function save():ByteArray")
    wi("{")
    @indent_level += 1
    @index = 0
    wi("var buffer:ByteArray = new ByteArray();")
		wi("var ps:PacketOutputStream = new PacketOutputStream(buffer);")
  end

  def save_scalar(scalar)
    name = camelcase(scalar.name)
    if(fbuiltin?(scalar.obj_type))
      wi("ps.save#{ftype2(scalar.obj_type)}(#{name});")
    else
      wi("ps.saveArray(#{name}.save());")
    end
  end

  def save_list(list)
    name = camelcase(list.name)
     ltype = ftype2(list.obj_type)
  
    if(list.obj_type == "uint8" || list.obj_type == "int8")
      wi("ps.saveInt(#{name}.length);")
      wi("ps.saveArray(#{name});")
    else
          # Save as List of Objects
      wi("ps.saveInt(#{name}.length);")
      if(@index == 0)
        wi("var i:int;")
        @index += 1
      end
      
      wi("for( i = 0; i != #{name}.length; i ++)")
      wi("{");
      #@indent_level += 1
      if(fbuiltin?(list.obj_type))
        wi("ps.save#{ltype}(#{name}[i]);",1)
      else
        wi("var _tmp_#{name}:ByteArray = #{name}[i].save();",1) 
        wi("ps.saveArray(_tmp_#{name});",1) 
      end
      wi("}");
    end
  end

  def start_load(struct)
    wi("")
    wi("public function load(buffer:ByteArray):void ")
    wi("{")
    @indent_level += 1
    @index = 0
    wi("var ps:PacketInputStream = new PacketInputStream(buffer);")
  end

  def load_enum(enum)
    name = enum.type.capitalize
    name = camelcase(name)
    wi("#{camelcase(enum.name)} = #{name}Enum.make#{name}Enum(ps.loadUnsignedByte());")
  end

  def save_enum(enum)
    name = camelcase(enum.name)
    wi("ps.saveUnsignedByte(#{name});")
  end

  def load_scalar(scalar)
    name = camelcase(scalar.name)
    if(fbuiltin?(scalar.obj_type))
      wi("#{name} = ps.load#{ftype2(scalar.obj_type)}();")
    else
      wi("#{name} = new #{ftype(scalar.obj_type)}();")
      wi("#{name}.load(buffer);")
    end
  end

  def load_list(list)
    name = camelcase(list.name)
    ltype = ftype2(list.obj_type)

    
    # Array of uint8 *is* byte
    if(list.obj_type == "uint8" || list.obj_type == "int8")
      wi("var #{name}Count:int = ps.loadInt();")
      wi("#{name} = ps.loadByteArray(#{name}Count);")
    
    elsif(fbuiltin?(list.obj_type))
      if ( @index == 0 )
        wi("var i:int;")
        @index += 1
      end
        
      wi("var #{name}Count:int = ps.loadInt();")
      wi("#{name} = new Array();")
      wi("for (i = 0; i < #{name}Count; i ++)")
      wi("{");
      wi("#{name}[i] = ps.load#{ltype}();",1)
      wi("}");
      
    else
     if ( @index == 0 )
        wi("var i:int;")
        @index += 1
      end
      
      wi("var #{name}Count:int = ps.loadInt();")
      wi("#{name} = new Array();")
      wi("for( i = 0; i < #{name}Count; i ++) {")
      if(fbuiltin?(list.obj_type))
        wi("#{name}[i] = ps.load#{ltype}();",1)
      else
        wi("var _tmp#{@index}:#{ltype}  = new #{ltype}();",1)
        wi("_tmp#{@index}.load(buffer);",1)
        wi("#{name}[i] = _tmp#{@index};",1)
      end
      wi("}")
      @index += 1
    end
  end

  def object_factory(structs, version)
    wi("import com.cubeia.firebase.io.ObjectFactory;")
    wi("")
    wi("public class ProtocolObjectFactory implements com.cubeia.firebase.io.ObjectFactory {")
    wi("public static function version():int {",1)
    wi("return #{version};",2)
    wi("}",1)
    wi("")
    wi("public function create(classId:int):ProtocolObject {",1)
    wi("switch(classId) {",2)

    structs.each do |struct|
      wi("case #{struct.class_id}:",3)
      wi("return new #{camelcasetype(struct.name)}();",4)
    end

    wi("}",2)
    wi("return null;",2)
    wi("}",1)
    wi("}")
  end
  
  def wi(line,extra = 0)
    if(!line.empty?)
      @out.write("    " * (@indent_level + extra))
    end
    @out.write(line + "\n")
  end

  def FlashOut.gen_enum_deserializer(name, values)
    name = name.capitalize
    name = camelcase(name)
    str = "\n        public static function make#{name}Enum(value:int):int  {\n"
    str<< "            switch(value) {\n"
    values.each_with_index {|v,i| str << "                case #{i}: return #{name}Enum.#{v};\n" }
    str<< "            }\n"
    str<< "            return -1;\n"
    str<< "        }\n\n"
    str
  end

  def FlashOut.gen_enum(f, name, values)
    name = name.capitalize
    name = camelcase(name)
    values.each_with_index {|v,i| f.write("        public static const #{v}:int = #{i};\n")}
  end

  def FlashOut.generate_enums(filename, enums, package)
    enums.each_pair do |enum, values|
      name = enum.capitalize
      name = camelcase(name)
      
      enum_file_name = "#{filename}#{name}Enum.as"
      File.open(enum_file_name, 'w') do |enum_file|
      enum_file.write(FlashOut.file_header(package))
      enum_file.write("    public final class #{name}Enum\n    {\n")
      gen_enum(enum_file, enum.capitalize, values)
      enum_file.write(gen_enum_deserializer(enum.capitalize, values))
      enum_file.write(FlashOut.file_footer(package))
      enum_file.write("    }\n")
      puts "Wrote #{enum_file_name}" unless $quiet
    end
    end
  end

  def FlashOut.file_header(package)
    <<HEADER_END
// I AM AUTO-GENERATED, DON'T CHECK ME INTO SUBVERSION (or else...)
package #{package} {

    import com.cubeia.firebase.io.PacketInputStream;
    import com.cubeia.firebase.io.PacketOutputStream;
    import com.cubeia.firebase.io.ProtocolObject;
  
    import flash.utils.ByteArray;

HEADER_END
  end

  def sysout(list)
    wi("\n")
    wi("public function toString():String")
    wi("{")
    wi("var result:String = \"#{camelcasetype(@classname)} :\";", 1)
    list.each do |member| 
          wi("result += \" #{member.name}[\"+#{camelcase(member.name)}+\"]\" ;", 1)
    end
    wi("return result;", 1)
    wi("}\n")
  end

def FlashOut.file_footer(package)
    <<FOOTER_END
}

// I AM AUTO-GENERATED, DON'T CHECK ME INTO SUBVERSION (or else...)

FOOTER_END
  end

end
