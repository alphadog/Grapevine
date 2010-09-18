module DBConnector
	def db
		@db ||= Sequel.connect(ENV['DATABASE_URL'] || 'sqlite://grapevine.db')
	end
end
