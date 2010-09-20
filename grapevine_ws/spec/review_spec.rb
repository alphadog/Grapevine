require File.join(File.dirname(__FILE__), '..', 'review') 

describe Review do

	let(:params){{:image_url => 'http://twitpic.com/eRR323', 
							 :text => 'paprika is a dark, shady and creepy place', 
							 :like => false,
							 :latitude => 303.121, 
							 :longitude => 102.22}}

	it "should be created if all attributes are present" do
		Review.create(params)
	end

	it "should not be created if all attributes are not present" do
		lambda { Review.create(:image_url => params[:image_url]) }.should raise_error
	end

	it "should return all reviews in range" do
		# 1 unit of geo-coordinate in decimal format ~ 100kms 
		far_on_right = Review.create(params.merge(:latitude => 18.768, :longitude => 75.889))
		within_range = Review.create(params.merge(:latitude => 18.516, :longitude => 73.916))
		far_twds_top = Review.create(params.merge(:latitude => 19.213, :longitude => 73.990))

		selected_reviews = Review.find_within_range(:latitude => 18.490, :longitude => 73.925, :range => 3)
		selected_reviews.first[:latitude].should  == 18.516
		selected_reviews.first[:longitude].should == 73.916
	end

	after(:each) do
		Review.dataset.delete
	end

end
