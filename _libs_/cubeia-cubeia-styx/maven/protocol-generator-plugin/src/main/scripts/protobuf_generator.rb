#
# C++ output writer
#

PROTOBUF_TYPES = {
  'int8'   => 'sint32',
  'uint8'  => 'int32',
  'int16'  => 'sint32',
  'uint16' => 'int32',
  'int32'  => 'sint32',
  'uint32' => 'int32',
  'int64'  => 'sint64',
  'uint64' => 'int64',
  'string' => 'string',
  'bool'   => 'bool'
}

class ProtobufOut
  def initialize(output_file)
    @indent_level = 0
    @field_index = 1;
    @out = output_file
  end

  def order
    [:start_class, :nested_types, :save, :load, :members, :end_class]
  end

  def protobuf_type(generic_type)
    PROTOBUF_TYPES[generic_type] || camelcasetype(generic_type)
  end

  def builtin?(generic_type)
    PROTOBUF_TYPES.has_key?(generic_type)
  end

def camelcasetype(str)
  str.split('_').map{|e| e.capitalize}.join
end

  def start_class(name, class_id)
    wi("message #{camelcasetype(name)} {")
    @field_index = 1;
    @indent_level += 1
  end

  def end_class
    @indent_level -= 1
    wi("}")
  end

  def end_scope
    @indent_level -= 1
    wi("}")
  end

  def member_scalar(scalar)
    wi("optional #{protobuf_type(scalar.obj_type)} #{scalar.name} = #{@field_index};")
    @field_index += 1
  end

  def member_list(list)
    if(list.obj_type == "uint8")
      wi("optional bytes #{list.name} = #{@field_index};")
    else 
      wi("repeated #{protobuf_type(list.obj_type)} #{list.name} = #{@field_index};")
    end
    @field_index += 1
  end

  def member_enum(enum)
    wi("optional #{camelcasetype(enum.type)}.Enum #{enum.name} = #{@field_index};");
    @field_index += 1
  end

  def start_save(struct)
  end

  def end_save
  end

  def save_scalar(scalar)
  end

  def save_list(list)
  end

  def save_enum(enum)
  end

  def start_load(struct)
  end

  def end_load
  end

  def load_scalar(scalar)
  end

  def load_list(list)
    end

  def load_enum(enum)
  end

  def wi(line,extra = 0)
    if(!line.empty?)
      @out.write("    " * (@indent_level + extra))
    end
    @out.write(line + "\n")
  end

  def ProtobufOut.file_footer
<<FOOTER_END

FOOTER_END
  end

  def ProtobufOut.enums
    str = ""
    $enums.each_pair do |enum_name, values|
      str << "message #{camelcasetype(enum_name)} {\n"
      str << "\tenum Enum {\n"
      i = 0
      values.each do |value|
        str << "\t\t#{value}=#{i};\n"
        i = i + 1
      end
      str << "\t}\n"
      str << "}\n"
    end
    str
  end

  def ProtobufOut.file_header(package)
<<HEADER_END
option java_package = "#{package}";

#{enums}

HEADER_END
  end
end
