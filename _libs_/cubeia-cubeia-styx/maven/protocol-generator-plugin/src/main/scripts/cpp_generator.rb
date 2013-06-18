#
# C++ output writer
#

CPP_TYPES = {
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

class CppOut
  def initialize(output_file)
    @indent_level = 0
    @out = output_file
  end

  def order
    [:start_class, :nested_types, :save, :load, :members, :end_class]
  end

  def ctype(generic_type)
    CPP_TYPES[generic_type] || generic_type
  end

  def builtin?(generic_type)
    CPP_TYPES.has_key?(generic_type)
  end

  def start_class(name, class_id)
    wi("struct #{name}")
    wi("{")
    @indent_level += 1
    if !class_id.to_s.empty?
      wi("enum { class_id = #{class_id} };")
      wi("")
    end
  end

  def end_class
    @indent_level -= 1
    wi("};")
  end

  def end_scope
    @indent_level -= 1
    wi("}")
  end

  def member_scalar(scalar)
    wi("#{ctype(scalar.obj_type)} #{scalar.name};")
  end

  def member_list(list)
    wi("std::vector<#{ctype(list.obj_type)}> #{list.name};")
  end

  def member_enum(enum)
    wi("#{enum.type}::enumeration #{enum.name};");
  end

  def start_save(struct)
    wi("friend styx_ostream & operator<<(styx_ostream & ps, const #{struct.name} & o)")
    wi("{")
    @indent_level += 1
  end

  def end_save
    wi("return ps;")
    @indent_level -= 1
    wi("}\n")
  end

  def save_scalar(scalar)
    wi("ps << o.#{scalar.name};")
  end

  def save_list(list)
    wi("ps << o.#{list.name};")
  end

  def save_enum(enum)
    wi("ps << static_cast<uint8_t>(o.#{enum.name});")
  end

  def start_load(struct)
    wi("friend styx_istream & operator>>(styx_istream & ps, #{struct.name} & o)")
    wi("{")
    @indent_level += 1
  end

  def end_load
    wi("return ps;")
    @indent_level -= 1
    wi("}\n")
  end

  def load_scalar(scalar)
    wi("ps >> o.#{scalar.name};")
  end

  def load_list(list)
    wi("ps >> o.#{list.name};")
  end

  def load_enum(enum)
    wi("uint8_t loadVar;")
    wi("ps >> loadVar;")
    wi("o.#{enum.name} = static_cast<#{enum.type}::enumeration>(loadVar);")
  end

  def wi(line,extra = 0)
    if(!line.empty?)
      @out.write("    " * (@indent_level + extra))
    end
    @out.write(line + "\n")
  end

  def CppOut.file_footer
<<FOOTER_END

} // namespace proto

#endif // U_AUTO_PROTOCOL_HPP
FOOTER_END
  end

  def CppOut.enums
    str = ""
    $enums.each_pair do |enum_name, values|
      str << "namespace #{enum_name} {\n"
      str << "\tenum enumeration { #{values.join ', '} };\n"
      str << "}\n"
    end
    str
  end

  def CppOut.file_header(version)
<<HEADER_END
#ifndef U_AUTO_PROTOCOL_HPP
#define U_AUTO_PROTOCOL_HPP

#include <string>
#include <vector>

#include "styx.hpp"
#include "styx_istream.hpp"
#include "styx_ostream.hpp"

#ifdef REGISTERED
	#undef REGISTERED
#endif

namespace proto {

const uint32_t version = #{version};

#{enums}
HEADER_END
  end
end
