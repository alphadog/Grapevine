require 'sequel'

class Review
	
	def initialize(a)
		raise Exception, "Not all attributes are provided." if (Review.required_attrs - a.keys).length != 0

		db = Sequel.connect('sqlite://db/grapevine.db')
		reviews = db.from(:reviews)
		reviews.insert(a)
	end

	def self.all
		db = Sequel.connect('sqlite://db/grapevine.db')
		reviews = db.from(:reviews).all
	end
	
	def self.find_in_range
	end

	def self.truncate; Sequel.connect('sqlite://db/grapevine.db').from(:reviews).delete; end 
	
	def self.required_attrs; [:image_url, :text, :latitude, :longitude]; end
	required_attrs.each {|a| attr_accessor a }

end
