require 'sinatra'
require 'json'

get '/' do 
	erb :index
end

post '/output' do 
	File.write("config.json", params.to_json)
	`java -cp "jars/*:" webservicediscovery/WebServiceDiscovery`
	output = File.read("result_advs.json")
end