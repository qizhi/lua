#
# Javascript output writer
#

JAVASCRIPT_TYPES = {
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

JAVASCRIPT_TYPES2 = {
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


def jstype(generic_type)
  JAVASCRIPT_TYPES[generic_type] || camelcasetype(generic_type)
end

def jstype2(generic_type)
  JAVASCRIPT_TYPES2[generic_type] || camelcasetype(generic_type)
end

def jsbuiltin?(generic_type)
  JAVASCRIPT_TYPES.has_key?(generic_type)
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

class JavaScriptOut
  
  
  def initialize(output_file, empty)
    @indent_level = 0
    @out = output_file
    @index = 0 
    @empty = empty
  end

  def order
    [:start_class, :nested_types, :members, :default_constructor, :save, :load, :sysout, :end_class]
  end

  def start_class(name, class_id)
    @classname = $javascript_namespace + "." + camelcasetype(name)

    @classid = "#{$javascript_namespace}.#{camelcasetype(name)}.CLASSID = #{class_id};\n"    
    
    wi("#{$javascript_namespace}.#{camelcasetype(name)} = function(){")
    @indent_level += 1
    if !class_id.to_s.empty?
      wi("this.classId = function() {")
      wi("return #{$javascript_namespace}.#{camelcasetype(name)}.CLASSID;",1)
      wi("};")
      wi("")
      
    end
  end
  
  def default_constructor()
    wi("")
  end  
    
  def end_scope
    @indent_level -= 1
    @index = 0
    wi("};")
  end

def end_save
     if !@empty
      wi("return byteArray;")
     else
       wi("return new FIREBASE.ByteArray();")
     end
     @indent_level -= 1
     wi("};")
  end

  def end_class
    @indent_level -= 1
    wi("};\n")
    wi("// CLASSID definition ")
    wi("#{@classid}")
  end
  
  alias :end_load :end_scope

  def member_scalar(scalar)
    type = jstype(scalar.obj_type)
    if(UNSIGNED_TYPES.has_key?(type))
      type = UNSIGNED_TYPES[type]
    end
    wi("this.#{camelcase(scalar.name)} = {}; // #{type};")
  end

  def member_list(list)
    wi("this.#{camelcase(list.name)} = [];")
  end

  def member_enum(enum)
    enumtype = enum.type
    enumtype = enumtype.capitalize
    enumtype= camelcase(enumtype)
    name = camelcase(enum.name)
    # wi("this.#{name} = {};")
    wi("this.#{name} =  #{$javascript_namespace}.#{enumtype}Enum.make#{enumtype}Enum(0);")
  end

  def start_save(struct)
    wi("")
    wi("this.save = function() {")
    @indent_level += 1
    @index = 0
    if !@empty
        wi("var byteArray = new FIREBASE.ByteArray();")
    end
  end

  def save_scalar(scalar)
    name = camelcase(scalar.name)
    if(jsbuiltin?(scalar.obj_type))
      wi("byteArray.write#{jstype2(scalar.obj_type)}(this.#{name});")
    else
      wi("byteArray.writeArray(this.#{name}.save());")
    end
  end

  def save_list(list)
    name = camelcase(list.name)
     ltype = jstype2(list.obj_type)
  
    if(list.obj_type == "uint8" || list.obj_type == "int8")
      wi("byteArray.writeInt(this.#{name}.length);")
      wi("byteArray.writeArray(this.#{name});")
    else
          # Save as List of Objects
      wi("byteArray.writeInt(this.#{name}.length);")
      if(@index == 0)
        wi("var i;")
        @index += 1
      end
      
      wi("for( i = 0; i < this.#{name}.length; i ++) {")
      #@indent_level += 1
      if(jsbuiltin?(list.obj_type))
        wi("byteArray.write#{ltype}(this.#{name}[i]);",1)
        
      elsif(enum?(list.obj_type))
       wi("byteArray.writeUnsignedByte(this.#{name}[i]); // ENUM Fixme",1)
        
      else
        wi("byteArray.writeArray(this.#{name}[i].save());",1) 
      end
      wi("}");
    end
  end

  def start_load(struct)
    wi("")
    wi("this.load = function(byteArray) {")
    @indent_level += 1
    @index = 0
#    if !@empty
#      wi("var byteArray = new FIREBASE.ByteArray(buffer);")
#    end
  end

  def load_enum(enum)
    name = enum.type.capitalize
    name = camelcase(name)
    wi("this.#{camelcase(enum.name)} =  #{$javascript_namespace}.#{name}Enum.make#{name}Enum(byteArray.readUnsignedByte());")
  end

  def save_enum(enum)
    name = camelcase(enum.name)
    wi("byteArray.writeUnsignedByte(this.#{name});")
  end

  def load_scalar(scalar)
    name = camelcase(scalar.name)
    if(jsbuiltin?(scalar.obj_type))
      wi("this.#{name} = byteArray.read#{jstype2(scalar.obj_type)}();")
    else
      wi("this.#{name} = new #{$javascript_namespace}.#{jstype(scalar.obj_type)}();")
      wi("this.#{name}.load(byteArray);")
    end
  end

  def load_list(list)
    name = camelcase(list.name)
    ltype = jstype2(list.obj_type)
    
    # Array of uint8 *is* byte
    if(list.obj_type == "uint8" || list.obj_type == "int8")
      wi("var #{name}Count = byteArray.readInt();")
      wi("this.#{name} = byteArray.readArray(#{name}Count);")
    
    elsif(jsbuiltin?(list.obj_type))
      if ( @index == 0 )
        wi("var i;")
        @index += 1
      end
        
      wi("var #{name}Count = byteArray.readInt();")
      wi("this.#{name} = [];")
      wi("for (i = 0; i < #{name}Count; i ++) {")
      wi("this.#{name}.push(byteArray.read#{ltype}());",1)
      wi("}");
      
    else
     if ( @index == 0 )
        wi("var i;")
        @index += 1
      end
      
      wi("var #{name}Count = byteArray.readInt();")
      
      if(!jsbuiltin?(list.obj_type))
        wi("var o#{ltype};");
      end
      wi("this.#{name} = [];")
      wi("for( i = 0; i < #{name}Count; i ++) {")
      if(jsbuiltin?(list.obj_type))
        wi("this.#{name}.push(byteArray.read#{ltype}());",1)
      elsif(enum?(list.obj_type))
      	wi("o#{ltype} =  #{$javascript_namespace}.#{ltype}Enum.make#{ltype}Enum(byteArray.readUnsignedByte());", 1)
      	wi("this.#{name}.push(o#{ltype});",1)
      else
        wi("o#{ltype} = new #{$javascript_namespace}.#{ltype}();",1)
        wi("o#{ltype}.load(byteArray);",1)
        wi("this.#{name}.push(o#{ltype});",1)
      end
      wi("}")
      @index += 1
    end
  end
  
  def object_factory(structs, version)
    wi("#{$javascript_namespace}.ProtocolObjectFactory = {};\n")
    
    wi("#{$javascript_namespace}.ProtocolObjectFactory.version = function() {")
       wi("return #{version};",1)
    wi("}")
       
    wi("#{$javascript_namespace}.ProtocolObjectFactory.create = function(classId, gameData) {")
    wi("var protocolObject;",1)
    wi("switch(classId) {",1)

    structs.each do |struct|
      wi("case #{$javascript_namespace}.#{camelcasetype(struct.name)}.CLASSID:",2)
      wi("protocolObject = new #{$javascript_namespace}.#{camelcasetype(struct.name)}();",3)
      wi("protocolObject.load(gameData);",3)
      wi("return protocolObject;",3)
    end

    wi("}",2)
    wi("return null;",1)
    wi("}")
  end

   def wi(line,extra = 0)
    if(!line.empty?)
      @out.write("    " * (@indent_level + extra))
    end
    @out.write(line + "\n")
  end

  def JavaScriptOut.gen_enum_deserializer(name, values)
    name = name.capitalize
    name = camelcase(name)
    str = "\n#{$javascript_namespace}.#{name}Enum.make#{name}Enum = function(value) {\n"
    str<< "    switch(value) {\n"
    values.each_with_index {|v,i| str << "        case #{i}: return #{$javascript_namespace}.#{name}Enum.#{v};\n" }
    str<< "    }\n"
    str<< "    return -1;\n"
    str<< "};\n"
    str
  end
  
def JavaScriptOut.gen_enum_toString(name, values)
   name = name.capitalize
   name = camelcase(name)
   str = "\n#{$javascript_namespace}.#{name}Enum.toString = function(value) {\n"
   str<< "    switch(value) {\n"
   values.each_with_index {|v,i| str << "        case #{i}: return \"#{v}\";\n" }
   str<< "    }\n"
   str<< "    return \"INVALID_ENUM_VALUE\";\n"
   str<< "};\n"
   str
 end

  def JavaScriptOut.gen_enum(f, name, values)
    name = name.capitalize
    name = camelcase(name)
    values.each_with_index {|v,i| f.write("    #{$javascript_namespace}.#{name}Enum.#{v} = #{i};\n")}
  end

  def JavaScriptOut.generate_enums(filename, enums, package)
    enums.each_pair do |enum, values|
      name = enum.capitalize
      name = camelcase(name)
      
      enum_file_name = "#{filename}#{name}Enum.js"
      File.open(enum_file_name, 'w') do |enum_file|
      enum_file.write(JavaScriptOut.file_header(package))
      enum_file.write("#{$javascript_namespace}.#{name}Enum = function() {\n")
      enum_file.write("};\n")
      gen_enum(enum_file, enum.capitalize, values)
      enum_file.write(gen_enum_deserializer(enum.capitalize, values))
      enum_file.write(gen_enum_toString(enum.capitalize, values))
      enum_file.write(JavaScriptOut.file_footer(package))
      puts "Wrote #{enum_file_name}" unless $quiet
    end
    end
  end

def JavaScriptOut.package_header(package)
 if package.include?"."
   parts = package.split(".")
   firstPart = parts[0]
   parts.slice!(0)
   nextPart = ""
   line1 = "var #{firstPart} = #{firstPart} || {};"
   line2 = ""
   namespace = ""
   parts.each do | part |
      nextPart += "." + part
      namespace = "#{firstPart}#{nextPart}"
      line2 += namespace + " = " + namespace + " || {};\n"
   end
 else
   namespace = package
   line1 = line1 = "var #{namespace} = #{namespace} || {};"
   line2 = ""
end

$javascript_namespace = namespace

<<HEADER_END
// I AM AUTO-GENERATED, DON'T CHECK ME INTO SUBVERSION (or else...)

#{line1}
#{line2}

HEADER_END

end

  
  def JavaScriptOut.file_header(package)
  end

  def sysout(list)
    wi("\n")
    wi("this.getNormalizedObject = function() {")
    wi("var normalizedObject = {};",1)
    wi("var i;",1)
    wi("normalizedObject.summary = \"#{@classname}\";",1)
    wi("normalizedObject.details = {};",1)
    list.each do |member|
     
      if member.kind_of?(ProtoList)
        wi("normalizedObject.details[\"#{camelcase(member.name)}\"] = [];",1)
        wi("for ( i = 0; i < this.#{camelcase(member.name)}.length; i ++ ) {",1)
        if  member.obj_type == "param" 
          wi("normalizedObject.details[\"#{camelcase(member.name)}\"].push(FIREBASE.Styx.getParam(this.#{camelcase(member.name)}[i]));",2)
        elsif enum?(member.obj_type)
          wi("normalizedObject.details[\"#{camelcase(member.name)}\"].push(#{$javascript_namespace}.#{camelcasetype(member.obj_type)}Enum.toString(this.#{camelcase(member.name)}[i]));",2)
        elsif jsbuiltin?(member.obj_type)
          wi("normalizedObject.details[\"#{camelcase(member.name)}\"].push(this.#{camelcase(member.name)}[i]);",2)
        else
          wi("normalizedObject.details[\"#{camelcase(member.name)}\"].push(this.#{camelcase(member.name)}[i].getNormalizedObject());",2)
        end
        wi("};",1)
      elsif member.kind_of?(ProtoEnum)
        wi("normalizedObject.details[\"#{camelcase(member.name)}\"] = #{$javascript_namespace}.#{camelcasetype(member.type)}Enum.toString(this.#{camelcase(member.name)});",1);
      else
          if member.obj_type && jsbuiltin?(member.obj_type)
            wi("normalizedObject.details[\"#{camelcase(member.name)}\"] = this.#{camelcase(member.name)};",1)
          else
            wi("normalizedObject.details[\"#{camelcase(member.name)}\"] = this.#{camelcase(member.name)}.getNormalizedObject();",1)
          end
      end
    end
    wi("return normalizedObject;",1)
    wi("};\n")
  end

def JavaScriptOut.file_footer(package)

   <<FOOTER_END

// I AM AUTO-GENERATED, DON'T CHECK ME INTO SUBVERSION (or else...)

FOOTER_END
end

end
