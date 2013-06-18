#
# Cpp output writer.
#

CPLUSPLUS_TYPES = {
  'int8'   => 'int8_t',
  'uint8'  => 'uint8_t',
  'int16'  => 'int16_t',
  'uint16' => 'uint16_t',
  'int32'  => 'int32_t',
  'uint32' => 'uint32_t',
  'int64'  => 'int64_t',
  'uint64' => 'uint64_t',
  'string' => 'std::string',
  'bool'   => 'bool'
}

CPLUSPLUS_TYPES2 = {
  'int8'   => 'int8_t',
  'uint8'  => 'uint8_t',
  'int16'  => 'int16_t',
  'uint16' => 'uint16_t',
  'int32'  => 'int32_t',
  'uint32' => 'uint32_t',
  'int64'  => 'int64_t',
  'uint64' => 'uint64_t',
  'string' => 'string',
  'bool'   => 'bool'
}

# Member type definitions, special handling for unsigned data types,


def cpptype2(generic_type)
  CPLUSPLUS_TYPES2[generic_type] || camelcasetype(generic_type)
end

def cpptype(generic_type)
  CPLUSPLUS_TYPES[generic_type] || generic_type
end

def builtin?(generic_type)
  CPLUSPLUS_TYPES.has_key?(generic_type)
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

class CPlusPlus
  def initialize(output_file, package_name, header, empty)
    @indent_level = 0
    @out = output_file
	@package_name = package_name
	@empty = empty
  end
  
  def order
    [:includes, :namespace, :start_class, :nested_types, :members, :default_constructor, :field_constructor, :save, :load, :sysout, :end_class]
  end
  
  def namespace()
	wi("namespace #{@package_name.gsub(/[.]/, '_')}")
	wi("{")
	wi("")
	@indent_level += 1
  end
  
  def start_class(name, class_id)
    @classname = camelcasetype(name)
    name = camelcasetype(name)
    static_tag = @indent_level != 1 ? ' static' : ''
    parent_tag = @indent_level == 1 ? ' : public styx::ProtocolObject' : ''
  
    
	wi("class #{name}#{parent_tag} {")
	wi("")
	wi("public:")
	wi("")
	@indent_level += 1
	if !class_id.to_s.empty?
	  wi("static const uint8_t CLASSID = #{class_id};\n")
	  wi("virtual uint8_t classId() {")
	  wi("return CLASSID;",1)
	  wi("}")
	  wi("")
	end

  end
  
  def includes(members)
     members.each do |member|
	   if member.is_a?(ProtoScalar) 
		  if !builtin?(member.obj_type)
			classname = camelcasetype(member.obj_type);
			wi("#include \"#{classname}.h\"")
		  end
		end
		
	  end
	  wi("")
	  wi("")
end
  
  def end_scope
    @indent_level -= 1
    wi("}")
  end
  
  def end_class
	wi("virtual void load(const styx::StyxBuffer &buffer)")
	wi("{")
	@indent_level += 1
	wi("styx::PacketInputStream packetInputStream(buffer);")
	wi("packetInputStream >> *this;")
	@indent_level -= 1
	wi("}\n")
	
	wi("virtual styx::StyxBuffer save(void) const")
	wi("{")
	@indent_level += 1
	wi("styx::PacketOutputStream packetOutputStream(CLASSID);")
	wi("packetOutputStream << *this;")
	wi("return packetOutputStream.packet();")
	@indent_level -= 1
	wi("}")

	@indent_level -= 1
	wi("};")
	@indent_level -= 1
	wi("}")
	wi("")
    wi("#endif")
	
  end
  
  
  #alias :end_save :end_scope
  alias :end_load :end_scope
  
  def member_scalar(scalar)
	if builtin?(scalar.obj_type)
	  type = cpptype(scalar.obj_type)
	else
	  type = camelcasetype(scalar.obj_type)
	end
	wi("#{type} #{scalar.name};")
	return type
	
  end
  
  def member_list(list)
	if (builtin?(list.obj_type))
	  type = cpptype(list.obj_type)
	else
	  type = camelcasetype(cpptype(list.obj_type))
	end
	name = list.name
	wi("std::vector<#{type}> #{name};")
	result = "std::vector<#{type}>"
	return result
  end
  
  def member_enum(enum)
	enumtype = camelcasetype(enum.type)
	name = enum.name
	wi("#{enumtype}::Enum #{name};")
	return "#{enumtype}::Enum"
  end
  
  
  def load_enum(enum)
	name = camelcasetype(enum.type)
	wi("{")
	@indent_level += 1
	wi("uint8_t temp;")
    wi("packetInputStream >> temp;")
	wi("#{camelcase(@savedstruct.name)}.#{camelcase(enum.name)} = static_cast<#{name}::Enum>(temp);")
                
	@indent_level -= 1
    wi("}")
	
  end
  
  def save_enum(enum)
	name = enum.name
	wi("buffer.write_uint8_t((uint8_t)#{name});")
  end
  
  def start_save(struct)
	@savedstruct = struct
	wi("friend styx::PacketOutputStream& operator<<(styx::PacketOutputStream &packetOutputStream, const #{camelcasetype(struct.name)} &#{camelcase(struct.name)})")
	wi("{")
	@indent_level += 1
	if @empty 
		wi("STYX_UNUSED(#{camelcase(struct.name)})")
	end
  end

  def end_save
    wi("packetOutputStream.finish();")
	wi("return packetOutputStream;")
	@indent_level -= 1
	wi("}\n")
  end

  def save_scalar(scalar)
	wi("packetOutputStream << #{camelcase(@savedstruct.name)}.#{scalar.name};")
  end

  def save_list(list)
		wi("packetOutputStream << #{camelcase(@savedstruct.name)}.#{list.name};")
  end

  def save_enum(enum)
	wi("packetOutputStream << static_cast<uint8_t>(#{camelcase(@savedstruct.name)}.#{enum.name});")
  end

  def start_load(struct)
	wi("friend styx::PacketInputStream & operator>>(styx::PacketInputStream &packetInputStream, #{camelcasetype(struct.name)} &#{camelcase(struct.name)})")
	wi("{")
	@indent_level += 1
	if @empty 
		wi("STYX_UNUSED(#{camelcase(struct.name)})")
	end
  end

  def end_load
	wi("return packetInputStream;")
	@indent_level -= 1
	wi("}")
  end

  def load_scalar(scalar)
    wi("packetInputStream >> #{camelcase(@savedstruct.name)}.#{scalar.name};")
  end
  
  def load_empty
	
  end

  def load_list(list)
	wi("packetInputStream >> #{camelcase(@savedstruct.name)}.#{list.name};")
  end
  
  def object_factory(structs, version)
    wi("class ProtocolObjectFactory {",1)
    wi("public:",1)
    wi("")
	wi("static styx::ProtocolObject* create(const styx::StyxBuffer &buffer)",2)
    wi("{",2)
	@indent_level += 1
    wi("const uint8_t *classId = reinterpret_cast<const uint8_t*>(&buffer[4]);",2)
    wi("return create(*classId);",2)
	@indent_level -= 1
    wi("}",2)
	wi("")
    wi("static styx::ProtocolObject* create(int classId) {",2)
    wi("switch(classId) {",3)
    
    structs.each do |struct|
      wi("case #{struct.class_id}:",4)
      wi("return new #{camelcasetype(struct.name)}();",5)
    end
	  wi("default:", 4)
	  wi("return NULL;",5);
    wi("}",3)

    wi("}",2)
    wi("};",1)
	wi("}")
	wi("")
	wi("#endif");
  end
  
  
  def wi(line,extra = 0)
    if(!line.empty?)
      @out.write("    " * (@indent_level + extra))
    end
    @out.write(line + "\n")
  end
  
  def CPlusPlus.gen_enum_deserializer(name, values)
    name = name.capitalize
    name = camelcase(name)
    str = "        static Enum make#{name}(int value) {\n"
    str<< "            switch(value) {\n"
    values.each_with_index {|v,i| str << "                case #{i}: return #{v};\n" }
	str<< "\n                default: return ENUM_ERROR;\n"
    str<< "            }\n"
    str<< "        }\n\n"
	str<< "        static Enum #{name}Error = make#{name}(ENUM_ERROR);\n\n"
    str
  end
  
  def CPlusPlus.gen_enum(name, values)
    name = name.capitalize
    name = camelcase(name)
    "        enum Enum { #{values.join(', ')}, ENUM_ERROR = -1 };\n\n"
  end
  
  def CPlusPlus.generate_enums(f, enums)
   # f.write("\nclass ProtocolEnums \n{\n\n")
   # f.write("private:\n")
   # f.write("    ProtocolEnums() {}\n\n")
   # f.write("public:\n")
    enums.each_pair do |enum, values|
      f.write("    namespace #{camelcase(enum.capitalize)}\n")
      f.write("    {\n")
      f.write(gen_enum(enum.capitalize, values))
      f.write(gen_enum_deserializer(enum.capitalize, values))
      f.write("    }\n")
    end
	f.write("}\n")
	f.write("\n")
	f.write("#endif\n");
  end
  
  def sysout(list)
	wi("")
  end
  
  def create_field_list(members)
  	result = ""
	members.each do |member|
	 result << "#{member.class_definition} #{member.name}, "
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
	wi("#{@classname}() {}")
	wi("")
	wi("#{@classname}(#{field_list}) {")
	members.each do |member|
	  var = member.name
	  wi("this->#{var} = #{var};", 1)      
	end
	wi("}\n")
  end
  
  def default_constructor()
  end  
  
  def CPlusPlus.file_header(package, name)
	define_value = name.gsub(/[.]/, '_')
	define_value += "_"
	define_value += name.hash.abs.to_s(16)
    <<HEADER_END
// I AM AUTO-GENERATED, DON'T CHECK ME INTO SUBVERSION (or else...)

#ifndef #{define_value.upcase}_INCLUDE
#define #{define_value.upcase}_INCLUDE

#include "styx_support.h"
#include "PacketInputStream.h"
#include "PacketOutputStream.h"
#include "ProtocolEnums.h"
#include "ProtocolObject.h"
HEADER_END
  
  end

def CPlusPlus.file(package, name)
    <<HEADER_END
// I AM AUTO-GENERATED, DON'T CHECK ME INTO SUBVERSION (or else...)

#include "styx_support.h"
#include "#{name}"
HEADER_END
  
end


def CPlusPlus.enum_file_header(package, name)
	uname = name + rand.to_s
 	define_value = name.gsub(/[.]/, '_')
	define_value += "_"
	define_value += uname.hash.abs.to_s(16)
	<<HEADER_END
// I AM AUTO-GENERATED, DON'T CHECK ME INTO SUBVERSION (or else...)

#ifndef #{define_value.upcase}_INCLUDE
#define #{define_value.upcase}_INCLUDE

#ifdef REGISTERED
	#undef REGISTERED
#endif

namespace #{package.gsub(/[.]/, '_')}
{
HEADER_END
  
  end

def CPlusPlus.file_header_factory(package,name)
 	define_value = name.gsub(/[.]/, '_')
	define_value += "_"
	define_value += name.hash.abs.to_s(16)
	<<HEADER_END
// I AM AUTO-GENERATED, DON'T CHECK ME INTO SUBVERSION (or else...)

#ifndef #{define_value.upcase}_INCLUDE
#define #{define_value.upcase}_INCLUDE

#include "styx_support.h"
#include "AllPackets.h"

namespace #{package.gsub(/[.]/, '_')}
{
HEADER_END
  
  end



end
