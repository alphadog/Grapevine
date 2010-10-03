require File.join(File.dirname(__FILE__), 'sequel_init')
require 'sequel'

DB.alter_table :reviews do
  add_column :location_name, :text
  add_column :username, :text
  add_column :created_at, :date
end

