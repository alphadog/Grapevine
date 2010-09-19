require File.join(File.dirname(__FILE__), '..', 'review.rb') 

describe Review do

	let(:params){{:image_url => 'http://twitpic.com/eRR323', 
							 :text => 'paprika is a dark, shady and creepy place', 
							 :like => false,
							 :latitude => 303.121, 
							 :longitude => 102.22}}

	it "should be created if all attributes are present" do
		Review.new(params)
	end

	it "should not be created if all attributes are not present" do
		lambda { Review.new(:image_url => params[:image_url]) }.should raise_error(Exception, "Not all attributes are provided.")
	end

	it "should return all reviews" do
		Review.new(params)
		Review.new(:image_url => 'http://twitpic.com/w', 
							 :text => 'sheesha is nice and cool',
							 :like => true,
							 :latitude => 343.341, 
							 :longitude => 122.313)
		
		Review.all[0][:text].should == 'paprika is a dark, shady and creepy place'
		Review.all[1][:text].should == 'sheesha is nice and cool'
	end

	it "should return a review given it's id" do
		Review.new(params.merge(:id => 33))
		 
		Review.find(33)[:text].should == params[:text]
	end

	it "should return all reviews in range" do
	pending

	end

	after(:each) do
		Review.truncate
	end

end
