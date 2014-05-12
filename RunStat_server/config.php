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


  // the name of the database.
  $database = "runstat_hostuju_cz";  
  // server to connect to.
  $server = "localhost";  
  // mysql username to access the database with.
  $db_user = "runstat.hostuju.cz"; 
  // mysql password to access the database with. 
  $db_pass = "runstatpswd";
  // the table that this script will set up and use.  
  $table = "users"; 
     
  $link = mysql_connect($server, $db_user, $db_pass);
  mysql_select_db($database,$link);
 
 
  error_reporting(E_ERROR);

  $description="ZČU RunStat";
  $keywords="ZČU RunStat";
  $title="ZČU RunStat";
  $header="<img src='data/icon.png' /> ZČU RunStat";
  $subtext="Simple application for running or walking";
  $footer="<span style='margin-right:5px;' class='glyphicon glyphicon-user'></span><span title='Ondřej Sadílek, Tomáš Bouda, Jan Janouškovec, Šárka Klímková & Patrik Kořínek'>Dream Team, 2014</span>";
  $message="<span style='color:#0D314D;'> Do you like it? If you are an android user, you can download it for free. App is available on </span><a href='https://github.com/sadileko/dream_team' class='alert-link'>Github</a>. ";
   
  
  $menu="  
  <li class='polozka'><a href='?q=home'><span class='glyphicon glyphicon-list'></span> History</a></li>
  <li class='polozka'><a href='?q=stats'><span class='glyphicon glyphicon-stats'></span> Stats</a></li>
   

         ";


?>
