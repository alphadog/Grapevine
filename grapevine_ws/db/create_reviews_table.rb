require 'sequel'

db = Sequel.connect('sqlite://grapevine.db')

db.create_table :reviews do
  primary_key :id
	String :image_url
	String :text
	Float :latitude
	Float :longitude
end
