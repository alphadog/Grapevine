require File.join(File.dirname(__FILE__), 'sequel_init') 

DB.create_table :reviews do
  primary_key :id
	String :image_url
	String :text
	TrueClass :like
	Float :latitude
	Float :longitude
end
