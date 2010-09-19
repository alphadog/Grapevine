require File.join(File.dirname(__FILE__), 'db', 'db_connector') 
require 'sequel'

class Review
	extend DBConnector

	def self.create(a)
		raise Exception, "Not all attributes are provided." if (Review.required_attrs - a.keys).length != 0

		reviews = Review.db.from(:reviews)
		reviews.insert(a)
	end

	def self.all
		reviews = db.from(:reviews).all
	end

	def self.find(id)
		db.from(:reviews).where(:id => id).first
	end
	
	def self.find_in_range
	end

	def self.truncate; db.from(:reviews).delete; end 
	
	def self.required_attrs; [:image_url, :text, :like, :latitude, :longitude]; end
	required_attrs.each {|a| attr_accessor a }

end
