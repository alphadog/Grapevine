require File.join(File.dirname(__FILE__), 'sequel_init')

DB.alter_table :reviews do
  add_column :location_name, :text
  add_column :username, :text
  add_column :created_at, :datetime
end

