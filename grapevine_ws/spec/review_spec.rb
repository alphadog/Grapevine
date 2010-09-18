describe Review do
	
	it "should be created if all attributes are present" do
		Review.new(:image_url => 'http://twitpic.com/eRR323', 
							 :text => 'paprika is a dark, shady and creepy place', 
							 :latitude => 303.121, 
							 :longitude => 102.22)
	end

	it "should not be created if all attributes are not present" do
		lambda { Review.new() }.should raise_error
		lambda { Review.new(:image_url => 'http://twitpic.com/eRR323') }.should raise_error
	end

end
