require File.join(File.dirname(__FILE__), 'sequel_init')
require 'faker'
require 'sequel'

reviews = DB.from(:reviews)

[ARGV.first.to_i, 15].max.times do
	reviews.insert(:image_url => "http://www.#{Faker::Internet.domain_name}/#{rand(99999)}",
								 :text => Faker::Lorem.paragraph[0..200],
								 :like => [true, false][rand(2)],
								 :latitude => 18.51667 + rand/10,
								 :longitude => 73.91667 + rand/10,
                 :location_name => Faker::Lorem.words.first,
                 :username => Faker::Name.first_name.downcase,
                 :created_at => Date.today.to_s)
end
