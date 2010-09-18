require 'sequel'

DB = Sequel.connect('sqlite://grapevine.db')

puts DB.inspect
