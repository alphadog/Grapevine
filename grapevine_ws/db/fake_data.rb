require File.join(File.dirname(__FILE__), 'db_connector') 
require 'faker'
require 'sequel'

include DBConnector

reviews = db.from(:reviews)

[ARGV.first.to_i, 15].max.times do
	reviews.insert(:image_url => "http://www.#{Faker::Internet.domain_name}/#{rand(99999)}",
								 :text => Faker::Lorem.paragraph[0..200],
								 :like => [true, false][rand(2)],
								 :latitude => 18.51667 + rand/10,
								 :longitude => 73.91667 + rand/10)
end
