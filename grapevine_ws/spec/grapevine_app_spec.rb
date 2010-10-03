require File.join(File.dirname(__FILE__), '..', '..', 'grapevine_app.rb')
require File.join(File.dirname(__FILE__), 'spec_helper')

describe "Grapevine App" do
  include Rack::Test::Methods

	def app; @app ||= Sinatra::Application; end
		
	let(:params){{:image_url => 'http://twitpic.com/eRR323', 
						 		:text => 'paprika is a dark, shady and creepy place', 
								:like => false,
						 		:latitude => 303.121, 
						 		:longitude => 102.22,
                :created_at => Date.today}}
	let(:token){{:token => 'e3e5f11e6c9cd54fc0fce481cf10f091'}}

	it "should return a 401 if token is absent" do
		get '/reviews', params

		last_response.status.should == 401
	end

	it "should list reviews" do
		r = Review.create(params)
	
		get '/reviews', token
		
		p last_response.body
		last_response.body.should =~ /paprika is a dark, shady and creepy place/
	end
	
	it "should return review details given a review id" do
		r = Review.create(params)

		get "/reviews/#{r.id}", token

		last_response.body.should =~ /paprika is a dark, shady and creepy place/
	end
	
	it "should create a review if parameters are correct" do
		post '/reviews', params.merge(token)

		last_response.status.should == 201
	end
	
	it "should not create a review if parameters are missing" do
		lambda { post '/reviews', params.reject {|k,v| k == :image_url}.merge(token) }.should raise_error
	end

	after(:each) do
		Review.dataset.delete
	end

end
