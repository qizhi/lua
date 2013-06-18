require 'rexml/document'
require 'pp'
require 'pathname'
require 'fileutils'

$enums = {}
$id_counter = 0
$class_ids = []

require 'java_generator'
require 'cpp_generator'
require 'flash_generator'
require 'csharp_generator'
require 'cplusplus_generator'
require 'protobuf_generator'
require 'javascript_generator'

def next_id
  $id_counter += 1
  while $class_ids.member?($id_counter)
    $id_counter += 1
  end
  $id_counter
end

class ProtoScalar
  attr_accessor :name, :obj_type, :class_definition
  
  def initialize(element)
    @name = element.attributes['name']
    @obj_type = element.attributes['type']
  end
  
  def save(lang_output)
    lang_output.save_scalar(self)
  end
  def load(lang_output)
    lang_output.load_scalar(self)
  end
  def write_member(lang_output)
    @class_definition = lang_output.member_scalar(self)
  end
end

class ProtoEnum
  attr_reader :name, :type, :class_definition
  
  def initialize(element)
    @name = element.attributes['name']
    @type = element.attributes['type']
  end
  
  def save(lang_output)
    lang_output.save_enum(self)
  end
  
  def load(lang_output)
    lang_output.load_enum(self)
  end
  
  def write_member(lang_output)
    @class_definition = lang_output.member_enum(self)
  end
end

class ProtoList < ProtoScalar

  def initialize(element)
    super(element)
  end
  
  def save(lang_output)
    lang_output.save_list(self)
  end
  def load(lang_output)
    lang_output.load_list(self)
  end
  def write_member(lang_output)
    @class_definition = lang_output.member_list(self)
  end
end

class ProtoStruct
  attr_accessor :name
  attr_accessor :members
  attr_accessor :nested_types
  attr_accessor :class_id
  
  def initialize(element)
    @name = element.attributes['name']
    @members = []
    @nested_types = []
    if element.attributes['id']
      id = element.attributes['id'].to_i
      $class_ids << id
      @class_id = id
    else
      @class_id = next_id
    end
    
    element.elements.each do |e|
      case e.name
        when 'var'
        if $enums.has_key?(e.attributes['type'])
          @members << ProtoEnum.new(e)
        else
          @members << ProtoScalar.new(e)
        end
        when 'list'
        @members << ProtoList.new(e)
        when 'struct'
        @nested_types << ProtoStruct.new(e)
      else
        raise Exception.new("unexpected element type '#{e.name}'")
      end
    end
  end
  
  def to_stream(lang_output,language)
    l = lang_output
    
    lang_output.order.each do |element|
      case element
		when :namespace
		lang_output.namespace
        when :start_class
        lang_output.start_class(name, class_id)
        when :includes
        lang_output.includes(@members)
        when :end_class
        lang_output.end_class
        when :default_constructor
        lang_output.default_constructor()        
        when :field_constructor
        lang_output.field_constructor(@members)
        when :nested_types
        @nested_types.each { |nested| nested.to_stream(l,language) }
        when :members
        @members.each { |member| member.write_member(l) }
        when :save
        l.start_save(self)
        @members.each { |member| member.save(l) }
        l.end_save
        when :load
        l.start_load(self)
    		@members.each { |member| member.load(l) }
    		if language == 'cplusplus' 
    			if @members.length == 0
    				l.load_empty
    			end
    		end
    		l.end_load
        when :sysout
        l.sysout(@members)
      end
    end
  end
end

def parse_enum(element)
  enum_name = element.attributes['name']
  if $enums[enum_name] != nil
    STDERR.puts "Redefining already defined enum #{enum_name}"
    exit!
  end
  
  $enums[enum_name] = []
  element.each_element do |child_element|
    if child_element.name.downcase != 'value'
      STDERR.puts "Invalid enum (#{enum_name}) child element: #{child_element.name}"
      exit!
    end
    
    $enums[enum_name] << child_element.text.upcase
  end
  
  if $enums[enum_name].empty?
    STDERR.puts "Enum #{enum_name} missing values!"
    exit!
  end
  
  if $enums[enum_name].size > 256
    STDERR.puts "Too many enums!"
    exit!
  end
end

# Driver
class CodeGenerator
  
  def initialize()
    $enums = {}
    $id_counter = 0
    $class_ids = []
  end

  def validate_dependency_order(structs, fail_on_bad_packet_order)
    type_name_array = structs.map {|struct| struct.name}
    error = false
    
    structs.each do |struct|
      struct.members.each do |member|
        if (member.kind_of?(ProtoScalar)  &&  type_name_array.index(member.obj_type) != nil)
          if type_name_array.index(member.obj_type) > type_name_array.index(struct.name)
            puts "WARNING: type '#{struct.name}' forward references (XML order) type '#{member.obj_type}', won't work in C++"
            error = true
          end
        end
      end
    end
    
    if (error  &&  fail_on_bad_packet_order)
      raise "bad packet order, failing build"
    end
  end
  
  def generate_code(protocol_file, language, output_dir, package_name, generate_visitors, version, fail_on_bad_packet_order, javascript_package_name)
    $quiet = false
    
    if(language != 'java' && language != 'cpp' && language != 'flash' && language != 'csharp' && language != 'cplusplus' && language != 'protobuf' && language != 'javascript')
      script_name = File.basename($0)
      puts "Usage:"
      puts "  java-mode: #{script_name} protocol.xml java  output-directory [package.name] [visitor]"
      puts " flash-mode: #{script_name} protocol.xml flash output-directory [package.name]"
      puts "   c++-mode: #{script_name} protocol.xml cpp   output-file"
      puts "csharp-mode: #{script_name} protocol.xml csharp output-directory [namespace.name]"
      puts "cplusplus  : #{script_name} protocol.xml cplusplus output-directory [namespace.name]"
      puts "protobuf   : #{script_name} protocol.xml protobuf output-directory"
	    puts "javascript : #{script_name} protocol.xml javascript output-directory [namespace.name]"
      exit(1)
    end
    
    begin
      file = File.new(protocol_file)
      doc = REXML::Document.new file
    rescue SystemCallError => ex
      puts "Couldn't open #{protocol_file}!"
      exit(1)
    end
    
    doc.root.each_element('enum') { |element| parse_enum(element) }
    structs = doc.root.elements.select {|e| e.name.downcase == 'struct' }.map { |element| ProtoStruct.new(element) }
    
    validate_dependency_order(structs, fail_on_bad_packet_order)      
      
    if (version != nil)
      puts "Using in parameter for version and ignoring anything specified in the xml file."
    elsif (doc.root.attribute('version') != nil && doc.root.attribute('version').value.match(/\$Revision: (\d+) \$/))
      puts "Using version from xml file"
      version = $1
    else
      puts "No version defined, exiting"
      exit(1)
    end
    
    begin
	if (language == 'javascript')
        package = javascript_package_name || package_name || "com.cubeia.firebase.protocol.generated"
        
        new_header_dir = create_javascript_header_dir(output_dir, package_name)
        puts 'Generating Javascript header to ' + new_header_dir unless $quiet
        FileUtils.mkdir_p(new_header_dir)

        file_name = "#{new_header_dir}/header.js"
        File.open(file_name,'w') do |outfile|
          outfile.write(JavaScriptOut.package_header(package))
        end
        
                
        new_output_dir = create_javascript_output_dir(output_dir, package_name)
        puts 'Generating Javascript code to ' + new_output_dir unless $quiet
        FileUtils.mkdir_p(new_output_dir)
        
        structs.each do |struct|
          
          class_name = camelcasetype(struct.name)
          file_name = "#{new_output_dir}/#{class_name}.js"
          
          File.open(file_name,'w') do |outfile|
            outfile.write(JavaScriptOut.file_header(package))
            struct.to_stream(JavaScriptOut.new(outfile,struct.members.empty?),language)
            outfile.write(JavaScriptOut.file_footer(package))
          end
          puts "Wrote #{file_name}" unless $quiet
        end
        
        # Object Factory
        factory_file_name = "#{new_output_dir}/ProtocolObjectFactory.js"
        File.open(factory_file_name, 'w') do |factory_file|
          factory_file.write(JavaScriptOut.file_header(package))
          JavaScriptOut.new(factory_file,false).object_factory(structs,version)
          factory_file.write(JavaScriptOut.file_footer(package))
        end
        puts "Wrote #{factory_file_name}" unless $quiet
        
        enum_file_name = "#{new_output_dir}/"
        JavaScriptOut.generate_enums(enum_file_name, $enums, package)

	elsif (language == 'flash')
        package = package_name || "com.cubeia.firebase.protocol.generated"
        
        new_output_dir = create_flash_output_dir(output_dir, package_name)
        
        puts 'Generating Flash code to ' + new_output_dir unless $quiet
        
        FileUtils.mkdir_p(new_output_dir)
        
        structs.each do |struct|
          
          class_name = camelcasetype(struct.name)
          file_name = "#{new_output_dir}/#{class_name}.as"
          
          File.open(file_name,'w') do |outfile|
            outfile.write(FlashOut.file_header(package))
            struct.to_stream(FlashOut.new(outfile),language)
            outfile.write(FlashOut.file_footer(package))
          end
          puts "Wrote #{file_name}" unless $quiet
        end
        
        # Object Factory
        factory_file_name = "#{new_output_dir}/ProtocolObjectFactory.as"
        File.open(factory_file_name, 'w') do |factory_file|
          factory_file.write(FlashOut.file_header(package))
          FlashOut.new(factory_file).object_factory(structs, version)
          factory_file.write(FlashOut.file_footer(package))
        end
        puts "Wrote #{factory_file_name}" unless $quiet
        
        enum_file_name = "#{new_output_dir}/"
        FlashOut.generate_enums(enum_file_name, $enums, package)
        
        
      elsif (language == 'java')
        package = package_name || "com.cubeia.firebase.protocol.generated"
        visitor = generate_visitors || ""
        visitor = true
        
        new_output_dir = create_java_output_dir(output_dir, package_name)
        
        puts 'Generating Java code to ' + new_output_dir unless $quiet
        
        FileUtils.mkdir_p(new_output_dir)
        
        puts 'Generated dir: ' + FileTest.exists?(new_output_dir).to_s
        
        structs.each do |struct|
          
          class_name = camelcasetype(struct.name)
          file_name = "#{new_output_dir}/#{class_name}.java"
          
          File.open(file_name,'w') do |outfile|
            outfile.write(JavaOut.file_header(package))
            struct.to_stream(JavaOut.new(outfile, visitor),language)
          end
          puts "Wrote #{file_name}" unless $quiet
        end
        
        # Object Factory
        factory_file_name = "#{new_output_dir}/ProtocolObjectFactory.java"
        File.open(factory_file_name, 'w') do |factory_file|
          factory_file.write(JavaOut.file_header(package))
          JavaOut.new(factory_file, visitor).object_factory(structs, version)
        end
        puts "Wrote #{factory_file_name}" unless $quiet
        
        # Object Visitor interface
        if (visitor)
          visitor_file_name = "#{new_output_dir}/PacketVisitor.java"
          File.open(visitor_file_name, 'w') do |visitor_file|
            visitor_file.write(JavaOut.file_header(package))
            JavaOut.new(visitor_file, visitor).object_visitor(structs, version)
          end
          puts "Wrote #{visitor_file_name}" unless $quiet
        end
        
        enum_file_name = "#{new_output_dir}/Enums.java"
        File.open(enum_file_name, 'w') do |enum_file|
          enum_file.write(JavaOut.file_header(package))
          JavaOut.generate_enums(enum_file, $enums)
        end
        puts "Wrote #{enum_file_name}" unless $quiet
        
      elsif (language == 'csharp')
        package = package_name || "Styx"
        
        new_output_dir = create_csharp_output_dir(output_dir, package_name)
        
        puts 'Generating C# code to ' + new_output_dir unless $quiet
        
        FileUtils.mkdir_p(new_output_dir)
        
        puts 'Generated dir: ' + FileTest.exists?(new_output_dir).to_s
        
        structs.each do |struct|
          
          class_name = camelcasetype(struct.name)
          file_name = "#{new_output_dir}/#{class_name}.cs"
          
          File.open(file_name,'w') do |outfile|
            outfile.write(CSharpOut.file_header(package))
            struct.to_stream(CSharpOut.new(outfile),language)
            outfile.write("}")
          end
          puts "Wrote #{file_name}" unless $quiet
        end
        
        # Object Factory
        factory_file_name = "#{new_output_dir}/ProtocolObjectFactory.cs"
        File.open(factory_file_name, 'w') do |factory_file|
          factory_file.write(CSharpOut.file_header(package))
          CSharpOut.new(factory_file).object_factory(structs, version)
          factory_file.write("}")
        end
        puts "Wrote #{factory_file_name}" unless $quiet
        
        enum_file_name = "#{new_output_dir}/Enums.cs"
        File.open(enum_file_name, 'w') do |enum_file|
          enum_file.write(CSharpOut.file_header(package))
          CSharpOut.generate_enums(enum_file, $enums)
           enum_file.write("}")
        end
        puts "Wrote #{enum_file_name}" unless $quiet
      elsif (language == 'cplusplus')
        package = package_name || "firebase"
        
        new_output_dir = create_cplusplus_output_dir(output_dir, package_name)
        
        puts 'Generating CPP code to ' + new_output_dir unless $quiet
        
        FileUtils.mkdir_p(new_output_dir)
        
        puts 'Generated dir: ' + FileTest.exists?(new_output_dir).to_s
        
        all_packet_headers_filename = "#{new_output_dir}/AllPackets.h"
        File.open(all_packet_headers_filename,'w') do |all_packets_outfile|
          all_packets_outfile.write("#pragma once\n\n")
		  structs.each do |struct|
		  
			all_packets_outfile.write("#include \"#{camelcasetype(struct.name)}.h\"\n")
            class_name =  camelcasetype(struct.name)
            file_name = "#{new_output_dir}/#{class_name}.h"
          
            File.open(file_name,'w') do |outfile|
              outfile.write(CPlusPlus.file_header(package, "#{class_name}.h" ))
              struct.to_stream(CPlusPlus.new(outfile,package,true,struct.members.empty?),language)
            end
			puts "Wrote #{file_name}" unless $quiet
			
          end
        end
        
        # Object Factory
        factory_file_name = "#{new_output_dir}/ProtocolObjectFactory.h"
        File.open(factory_file_name, 'w') do |factory_file|
          factory_file.write(CPlusPlus.file_header_factory(package, "ProtocolObjectFactory.h"))
          CPlusPlus.new(factory_file,package,true,false).object_factory(structs, version)
        end
        
        puts "Wrote #{factory_file_name}" unless $quiet
        
        enum_file_name = "#{new_output_dir}/ProtocolEnums.h"
        File.open(enum_file_name, 'w') do |enum_file|
          enum_file.write(CPlusPlus.enum_file_header(package, "ProtocolEnums.h"))
          CPlusPlus.generate_enums(enum_file, $enums)
        end
        puts "Wrote #{enum_file_name}" unless $quiet
      elsif (language == 'protobuf')
        package = package_name || "com.cubeia.firebase.protocol.generated"
        
        puts 'Generating protobuf source (protocol.proto)' unless $quiet
        
        new_output_dir = create_protobuf_output_dir(output_dir, "protocol")
        
        FileUtils.mkdir_p(new_output_dir)
        puts 'Generated dir: ' + FileTest.exists?(new_output_dir).to_s
        
        file_name = "#{new_output_dir}/protocol.proto"
        File.open(file_name,'w') do |outfile|
          outfile.write(ProtobufOut.file_header(package))
          protobufout = ProtobufOut.new(outfile)
          
          structs.each do |struct|
            struct.to_stream(protobufout,language)
            outfile.puts
          end
          outfile.write(ProtobufOut.file_footer)
        end
        puts "Wrote #{file_name}" unless $quiet
      else
        puts 'Generating C++ code' unless $quiet
        
        new_output_dir = create_cpp_output_dir(output_dir, package_name)
        
        FileUtils.mkdir_p(new_output_dir)
        puts 'Generated dir: ' + FileTest.exists?(new_output_dir).to_s
        
        file_name = "#{new_output_dir}/protocol.hpp"
        File.open(file_name,'w') do |outfile|
          outfile.write(CppOut.file_header(version))
          cppout = CppOut.new(outfile)
          
          structs.each do |struct|
            struct.to_stream(cppout,language)
            outfile.puts
          end
          
          outfile.write(CppOut.file_footer)
        end
        puts "Wrote #{file_name}" unless $quiet
      end
    rescue SystemCallError => ex
      puts "Caught exception: #{ex}"
      exit(1)
    end
  end
  
  def create_cplusplus_output_dir(base_name, package_name)
    result = base_name + '/' + package_name.gsub(/[\.]/, '/')
  end
  
  
  def create_csharp_output_dir(base_name, package_name)
    result = base_name + '/' + package_name.gsub(/[\.]/, '/')
  end
  
  def create_java_output_dir(base_name, package_name)
    result = base_name + '/' + package_name.gsub(/[\.]/, '/')
  end
  
  def create_flash_output_dir(base_name, package_name)
    puts "base = " + base_name
    result = base_name + '/' + package_name.gsub(/[\.]/, '/')
  end
  
  def create_cpp_output_dir(base_name, package_name)
    puts "C++ Base = " + base_name
    result = base_name + '/proto/'
  end
  
  def create_protobuf_output_dir(base_name, package_name)
    puts "protobuf base = " + base_name
    result = base_name
  end

  def create_javascript_output_dir(base_name, package_name)
    puts "javascript base = " + base_name
    result = base_name + '/script'
  end
  
  def create_javascript_header_dir(base_name, package_name)
      puts "javascript header = " + base_name
      result = base_name + '/header'
  end
  
end