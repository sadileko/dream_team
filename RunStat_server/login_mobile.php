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


	$host='localhost';
	$uname='runstat.hostuju.cz';
	$pwd='runstatpswd';
	$db="runstat_hostuju_cz";
  $table="users";
            
	$con = mysql_connect($host,$uname,$pwd) or die("connection failed");
	mysql_select_db($db,$con) or die("db selection failed");
	mysql_query("SET NAMES utf8");
  
  $username=$_REQUEST['nick'];
  $password=$_REQUEST['password'];


	$flag['code']=0;
  
  $r = mysql_query("SELECT id FROM $table WHERE username='$username' AND password='$password'",$con);
  $rows = mysql_num_rows($r);
  
	if($rows>0)
	{
		$flag['code']=1;
	}
  
    
	print(json_encode($flag));
	mysql_close($con);
?>