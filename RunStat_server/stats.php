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


  include("session.php");
  
    $logged = $_SESSION['user'];
    $result = mysql_query("SELECT * FROM users WHERE username='$logged'",$link);
    $row=mysql_fetch_array($result, MYSQL_ASSOC);  
    $role=$row['role'];
    $log_id = $row['id'];
    
    $result = mysql_query("SELECT MAX(id), run_id, run_type, MAX(time), MIN(time), MAX(steps), AVG(speed), MAX(distance) FROM `locations` GROUP BY run_id",$link);
    $pocet_behu = 0;
    $celkovy_cas = 0;
    $celkova_vzdalenost = 0;
    $prumerna_rychlost = 0;
  
              
    while($row=mysql_fetch_array($result, MYSQL_ASSOC)){ 
    
    $pocet_behu++;   
    $celkovy_cas += $row['MAX(time)'];
    $celkova_vzdalenost += $row['MAX(distance)'];
    $prumerna_rychlost = 0; 

     
    $cas = $row['MAX(time)']-$row['MIN(time)'];  
    $a_id=$row['run_id'];
              
    //echo "<tr><td>".$row['run_id']."</td><td><a href='index.php?q=graf&id=".$row['MAX(id)']."'><strong>".typBehu($row['run_type'])."</strong></a></td><td>".$cas."</td><td>".$row['MAX(distance)']."</td><td class='text-center'><a href='?q=home&a_del=$a_id' class='btn btn-danger btn-xs'><span class='glyphicon glyphicon-remove'></span> Odstranit</a></td></tr>"; 
    } 

    
?>
 <h3>Vyhodnocení</h3>
<div class="container">
    <div class="row  custyle">
    <table class="table table-striped custab">
 
 

<tr><td>Počet běhů:</td><td><?php echo $pocet_behu; ?></td></tr>
<tr><td>Celkový čas:</td><td><?php prevodCasu($celkovy_cas); ?></td></tr>
<tr><td>Celková vzdálenost:</td><td><?php printf('%02d m', $celkova_vzdalenost);  ?></td></tr>

    </table>
    </div>
    
</div>

<?php

 function prevodCasu( $input )
 {

  $uSec = $input % 1000;
  $input = floor($input / 1000);

  $seconds = $input % 60;
  $input = floor($input / 60);

  $minutes = $input % 60;
  $input = floor($input / 60);

  $hours = $input % 24;
  $input = floor($input / 24);  

	printf('%02d:%02d:%02d ', $hours, $minutes, $seconds);

	//return $hours.":".$minutes.":".$seconds;
 }

	
  