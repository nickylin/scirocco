# This file is auto-generated from the current state of the database. Instead of editing this file, 
# please use the migrations feature of Active Record to incrementally modify your database, and
# then regenerate this schema definition.
#
# Note that this schema.rb definition is the authoritative source for your database schema. If you need
# to create the application database on another system, you should be using db:schema:load, not running
# all the migrations from scratch. The latter is a flawed and unsustainable approach (the more migrations
# you'll amass, the slower it'll run and the greater likelihood for issues).
#
# It's strongly recommended to check this file into your version control system.

ActiveRecord::Schema.define(:version => 20111205080412) do

  create_table "projects", :force => true do |t|
    t.string   "name"
    t.datetime "created_at"
    t.datetime "updated_at"
  end

  create_table "summaries", :force => true do |t|
    t.integer  "project_id"
    t.string   "class_name"
    t.string   "device"
    t.string   "date"
    t.integer  "pass"
    t.integer  "fail"
    t.datetime "created_at"
    t.datetime "updated_at"
  end

  create_table "test_cases", :force => true do |t|
    t.integer  "project_id",            :null => false
    t.string   "class_name",            :null => false
    t.string   "device",                :null => false
    t.string   "date",                  :null => false
    t.string   "result"
    t.string   "test_name"
    t.text     "test_procedure"
    t.text     "confirmation_contents"
    t.text     "trace"
    t.string   "screen_shot_path"
    t.datetime "created_at"
    t.datetime "updated_at"
    t.text     "memory_info"
  end

end
