require File.join(File.dirname(__FILE__), '..', '..', 'grapevine_app.rb')
require 'rack/test'

describe "Grapevine App" do
  include Rack::Test::Methods

	def app; @app ||= Sinatra::Application; end
		
	let(:params){{:image_url => 'http://twitpic.com/eRR323', 
						 	:text => 'paprika is a dark, shady and creepy place', 
							:like => false,
						 	:latitude => 303.121, 
						 	:longitude => 102.22,
							:token => 'e3e5f11e6c9cd54fc0fce481cf10f091'}}

	it "should return a 401 if token is absent" do
		get '/reviews', params.reject {|k,v| k == :token}

		last_response.status.should == 401
	end

	it "should list reviews" do
		Review.should_receive(:all).and_return(mock('reviews', :to_json => true))

		get '/reviews', :token => params[:token]
	end
	
	it "should return review details given a review id" do
		Review.should_receive(:find).with('3').and_return(mock('review', :to_json => true))

		get '/reviews/3', :token => params[:token]
	end
	
	it "should create a review if parameters are correct" do
		post '/reviews', params

		last_response.status.should == 201
	end
	
	it "should not create a review if parameters are incorrect" do
		lambda { post '/reviews', params.reject {|k,v| k == :image_url} }.should raise_error
	end

	after(:each) do
		Review.truncate
	end

end
