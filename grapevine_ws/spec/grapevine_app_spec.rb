require File.join(File.dirname(__FILE__), '..', '..', 'grapevine_app.rb')
require 'rack/test'

describe "Grapevine App" do
  include Rack::Test::Methods

	def app; @app ||= Sinatra::Application; end

	it "should list reviews" do
		Review.should_receive(:all).and_return(mock('reviews', :to_json => true))

		get '/reviews'
	end
	
	it "should create a review if parameters are correct" do
		params = {:image_url => 'http://twitpic.com/eRR323', 
						 	:text => 'paprika is a dark, shady and creepy place', 
						 	:latitude => 303.121, 
						 	:longitude => 102.22}

		post '/reviews', params
	end
	
	it "should not create a review if parameters are incorrect" do
		incomplete_params = {:image_url => 'http://twitpic.com/eRR323'}
		
		lambda { post '/reviews', incomplete_params }.should raise_error
	end

	after(:each) do
		Review.truncate
	end

end
