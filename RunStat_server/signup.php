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

include("config.php"); 
//including config.php in our file

if(!empty($_POST['username']) && !empty($_POST['password']) && !empty($_POST['firstname']) && !empty($_POST['organization']) 
&& !empty($_POST['email'])){
// Now checking user name and password is entered or not.
$first_name= mysql_real_escape_string($_POST['firstname']);
$username = mysql_real_escape_string(stripslashes($_POST['username']));
$password = mysql_real_escape_string(stripslashes(md5($_POST['password'])));
$mail = mysql_real_escape_string($_POST['email']);
$organization= mysql_real_escape_string($_POST['organization']);
$check = "SELECT * from users where username = '".$username."'";
$qry = mysql_query($check);
$num_rows = mysql_num_rows($qry); 

if($num_rows > 0){
// Here we are checking if username is already exist or not.

echo "Uživatelské jméno již existuje.";
echo '<a href="index.php?q=signup">Registrovat znovu</a>';
exit;
}

// Now inserting record in database.
$query = "INSERT INTO users (firstname,organization,username,password,email,is_active,role) VALUES ('".$first_name."','".$organization."','".$username."','".$password."','".$mail."','1','1');";
mysql_query($query);
echo "Děkujeme za registraci.";
echo '<a href="index.php?q=login">Klikněte zde</a> pro přihlášení.';
exit;
}

?>

<form action="<?php $_SERVER['PHP_SELF']?>" method="post" class="form-horizontal">
<h3>Registrace</h3>
 <table>
    <tr>
      <td><label class="col-sm-2 control-label">Jméno</label></td> 
      <td><div class="col-sm-10"> <input type="text" class="form-control" name="firstname" size="20" placeholder="First name"></div></td>
    </tr>
		 
    <tr>
      <td><label class="col-sm-2 control-label">Nick</label></td>
      <td><div class="col-sm-10"> <input type="text" class="form-control" name="username" size="20" placeholder="User name"></div></td>
    </tr>
             
    <tr>
      <td><label class="col-sm-2 control-label">Heslo</label></td>
      <td><div class="col-sm-10"><input type="password" class="form-control" name="password" size="20" placeholder="Password"></div></td>
     </tr>
     
	 <tr>
      <td><label class="col-sm-2 control-label">Email</label></td>
      <td><div class="col-sm-10"> <input type="text" class="form-control" name="email" size="20" placeholder="Email"></div></td>
    </tr>
    
    <tr>
      <td><label class="col-sm-2 control-label">Organizace</label></td>
      <td><div class="col-sm-10"> <input type="text" class="form-control" name="organization" size="20" placeholder="Organization"></div></td>
    </tr>
    
	 <tr>
       <td><label class="col-sm-2 control-label"><input type="submit" value="Registrovat" class="btn btn-primary"></label></td>
        
     </tr>
    
 </table>
  <br>
</form>