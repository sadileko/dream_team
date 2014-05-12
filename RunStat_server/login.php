<?php 

/***********************************************************************************************************************
*
* This file is part of the RunStat project
* ==========================================
*
* Copyright (C) 2014 by University of West Bohemia (http://www.zcu.cz/en/)
*
***********************************************************************************************************************
*
* Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
* the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
* an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
* specific language governing permissions and limitations under the License.
*
***********************************************************************************************************************
*
* Dream team, 2014/5/11 Jan Janouskovec
*
**********************************************************************************************************************/



if(isset($_POST) && !empty($_POST))
{
session_start();
include("config.php"); //including config.php in our file
$username = mysql_real_escape_string(stripslashes($_POST['username'])); //Storing username in $username variable.
$password = mysql_real_escape_string(stripslashes(md5($_POST['password']))); //Storing password in $password variable.


$match = "select id from $table where username = '".$username."' and password = '".$password."';"; 

$qry = mysql_query($match);

$num_rows = mysql_num_rows($qry); 



if ($num_rows <= 0) { 


echo "<script>alert('Login $username se neshoduje se zadaným heslem.');</script>";
echo "<a href='?q=home'>Přihlašte se znovu</a>";

exit; 

} else {



$_SESSION['user']= $_POST["username"];
header("location:?q=home");
// It is the page where you want to redirect user after login.
}
}else{
?>

<div class="container">
    <div class="row">
		<div class="span12">
			<form class="form-horizontal" action="<?php $_SERVER['PHP_SELF'] ?>" method="POST" class="form-signin" id = "login_form">
			  <fieldset>
			    <h3>Login</h3>
			    <div class="control-group">
			      <!-- Username -->
			      <label class="control-label"  for="username">Username</label>
			      <div class="controls">
			        <input type="text" id="username" name="username" placeholder="Username" class="form-control">
			      </div>
			    </div>
			    <div class="control-group">
			      <!-- Password-->
			      <label class="control-label" for="password">Password</label>
			      <div class="controls">
			        <input type="password" id="password" name="password" placeholder="Password" class="form-control">
			      </div>
			    </div>
			    <div class="control-group">
			      <!-- Button -->
			      <div class="controls">
            <br>
			        <button class="btn btn-success">Login</button> <a style='margin-left:20px' href="?q=signup">Register</a> 
			      </div>
			    </div>
          
          
          
			  </fieldset>
			</form>
		</div>
	</div>
</div>

<br> 


<div class="alert alert-warning">
  
  <span style="color:black;">RunStat is a simple application used for "recording" running or walking. During the running provides current speed, distance and user location. It provides also history of user runnings, where is stored location, date, duration, max. and average speed, distance. Saved runnings can be also drawed on a map. Additionally these saved runnings can be synchronized with server, where user can see additional data, such is graphs and maps. </span>
</div>





<?php

}
?>