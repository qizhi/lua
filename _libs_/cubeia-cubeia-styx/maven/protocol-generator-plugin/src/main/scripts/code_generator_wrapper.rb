# top level function wrapper for the code generator
require 'generate_source'

def generate_code(protocol_file, language, output_dir, package_name, generate_visitors, version, fail_on_bad_packet_order, javascript_package_name)
  CodeGenerator.new.generate_code(protocol_file, language, output_dir, package_name, generate_visitors, version, fail_on_bad_packet_order, javascript_package_name)
end
