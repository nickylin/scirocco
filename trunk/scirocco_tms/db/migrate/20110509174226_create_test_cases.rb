class CreateTestCases < ActiveRecord::Migration
  def self.up
    create_table :test_cases do |t|
      t.references :project, :null => false
      t.string :class_name, :null => false
      t.string :device, :null => false
      t.string :date, :null => false
      t.string :result
      t.string :test_name
      t.text :test_procedure
      t.text :confirmation_contents
      t.text :trace
      t.string :screen_shot_path
      t.timestamps
    end
  end

  def self.down
    drop_table :test_cases
  end
end
