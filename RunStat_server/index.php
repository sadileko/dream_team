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

?>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html>
  <head>
    <?php include 'config.php'; ?>
    <meta http-equiv="content-type" content="text/html; charset=utf-8" />
    <meta name="description" content="<?php echo $description; ?>">
    <meta name="keywords" content="<?php echo $keywords; ?>">
    <link href="css/bootstrap.min.css" rel="stylesheet">
    <link href="css/sticky-footer.css" rel="stylesheet">
    <title><?php echo $title; ?></title>
  </head>
  
  <body>
  <div id="wrap">
   <div id="main-content" role="main">
    <div class="row">
      <div class="col col-sm-12" id="header">
        <h1><a href="index.php"><?php echo $header; ?></a></h1>
  		    <p class="lead"><?php echo $subtext; ?></p>               
          
          <div class="alert alert-info">
            <p><?php echo $message; ?></p>
          </div>
          
      </div>
    </div>
    
    <div class="row">
        <div class="col col-sm-3" id="column-1">
            <ul class="nav nav-pills nav-stacked">
            
              <?php echo $menu;  ?>
            </ul>
        </div>
    
        <div class="col col-sm-9" id="column-2">
          <?php if(isset($_GET['q'])){$q=$_GET['q'];} else{$q='home';} ?>
          <?php include $q.".php"; ?> 
        </div>
      
      </div>
      
      <div id="footer">
        <div class="container">
          <p class="text-muted credit"><?php echo $footer; ?></p>
        </div>
      </div>
      
    </div>
  </div>  
  </body>
</html>
<noscript>