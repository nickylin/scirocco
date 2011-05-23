class CreateSummaries < ActiveRecord::Migration
  def self.up
    create_table :summaries do |t|
      t.references :project
      t.string :class_name
      t.string :device
      t.string :date
      t.integer :pass
      t.integer :fail

      t.timestamps
    end
  end

  def self.down
    drop_table :summaries
  end
end
