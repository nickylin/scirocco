class TestCasesController < ApplicationController
  # GET /test_cases
  # GET /test_cases.xml
  def index

    @p_class = params[:class]
    @p_device = params[:device]
    @p_date = params[:date]
    @project = Project.find(params[:project_id])
    @cond_class_names = {"--select--" => ""}
    Summary.find(:all,:select => "distinct class_name",:conditions => ["project_id = ?",@project.id]).each do |sum|
      @cond_class_names[sum.class_name] = sum.class_name
    end

    @cond_class_names = {"--select--" => ""}
    Summary.find(:all,:select => "distinct class_name",:conditions => ["project_id = ?",@project.id]).each do |sum|
      @cond_class_names[sum.class_name] = sum.class_name
    end

    @cond_devices = {"--select--" => ""}
    if @p_class != nil
      Summary.find(:all,:select => "distinct device",:conditions => ["project_id = ? and class_name = ?",@project.id,@p_class]).each do |sum|
        @cond_devices[sum.device] = sum.device
      end
    end

    @cond_date = {"--select--" => ""}
    p @p_device
    if @p_device != nil
      Summary.find(:all,:select => "distinct date",
                   :conditions => ["project_id = ? and class_name = ? and device = ?",
                   @project.id , @p_class , @p_device ], 
                   :order => "date desc").each do |sum|
        @cond_date[sum.formatted_date] = sum.date
                   end
    end

    if (  @p_class != nil && @p_class != ""  && @p_device != nil && @p_device != ""  && @p_date != nil && @p_date != "" )
      @test_cases = TestCase.find(:all, :conditions => ["project_id = ? and class_name = ? and device = ? and date = ?",
                                  @project.id ,  @p_class , @p_device , @p_date])
      @summary = Summary.find(:first, :conditions => ["project_id = ? and class_name = ? and device = ? and date = ?",
                                  @project.id ,  @p_class , @p_device , @p_date])
    end


    respond_to do |format|
      format.html # index.html.erb
      format.xml  { render :xml => @test_cases }
    end
  end

  # GET /test_cases/1
  # GET /test_cases/1.xml
  def show
    @test_case = TestCase.find(params[:id])

    respond_to do |format|
      format.html # show.html.erb
      format.xml  { render :xml => @test_case }
    end
  end

  # GET /test_cases/new
  # GET /test_cases/new.xml
  def new
    @test_case = TestCase.new

    respond_to do |format|
      format.html # new.html.erb
      format.xml  { render :xml => @test_case }
    end
  end

  # GET /test_cases/1/edit
  def edit
    @test_case = TestCase.find(params[:id])
  end

  # POST /test_cases
  # POST /test_cases.xml
  def create
    @test_case = TestCase.new(params[:test_case])

    respond_to do |format|
      if @test_case.save
        format.html { redirect_to(@test_case, :notice => 'TestCase was successfully created.') }
        format.xml  { render :xml => @test_case, :status => :created, :location => @test_case }
      else
        format.html { render :action => "new" }
        format.xml  { render :xml => @test_case.errors, :status => :unprocessable_entity }
      end
    end
  end

  # PUT /test_cases/1
  # PUT /test_cases/1.xml
  def update
    @test_case = TestCase.find(params[:id])

    respond_to do |format|
      if @test_case.update_attributes(params[:test_case])
        format.html { redirect_to(@test_case, :notice => 'TestCase was successfully updated.') }
        format.xml  { head :ok }
      else
        format.html { render :action => "edit" }
        format.xml  { render :xml => @test_case.errors, :status => :unprocessable_entity }
      end
    end
  end

  # DELETE /test_cases/1
  # DELETE /test_cases/1.xml
  def destroy
    @test_case = TestCase.find(params[:id])
    @test_case.destroy

    respond_to do |format|
      format.html { redirect_to(test_cases_url) }
      format.xml  { head :ok }
    end
  end
end
