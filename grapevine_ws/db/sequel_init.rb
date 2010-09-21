require 'sequel'

DB = Sequel.connect(ENV['DATABASE_URL'] || 'sqlite://grapevine.db')
