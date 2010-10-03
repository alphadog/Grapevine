require File.dirname(__FILE__) + '/db/sequel_init'

class Review < Sequel::Model
	plugin :validation_helpers

	def validate
		super
		validates_presence [:text, :like, :latitude, :longitude]
	end

	def self.find_within_range(c)
		conversion_factor = 100.0
		c.keys.each {|k| c[k] = c[k].to_f }
		distance = c[:range] / conversion_factor

		max_latitude = c[:latitude] + distance
		min_latitude = c[:latitude] - distance

		max_longitude = c[:longitude] + distance
		min_longitude = c[:longitude] - distance

		filter(:latitude => min_latitude..max_latitude, :longitude => min_longitude..max_longitude)
	end
	
	def to_hash
		columns.inject({}) { |h, c| h[c] = self.send(c); h }
	end

end
