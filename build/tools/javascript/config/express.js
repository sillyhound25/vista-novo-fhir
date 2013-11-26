// create express object to be returned
var express  = require('express');
var provider = require('./mongoose.js');
var cons      = require('consolidate')

// create app object and setup as exported member of module
var app = express();

//configure app
// assign the swig engine to .html files
app.engine('eco', cons.eco);

// set .html as the default extension 
app.set('view engine', 'eco');
app.set('views', __dirname + '/../app/views');





// setup routes

//show for model
app.get('/:model/:id/:vid', function(req,res){
   var controller = require('../app/controllers/' + req.params.model)
   controller.load(req,res,req.params.id,req.params.vid, function(obj) {
      if(obj.constructor.name=="Error")
      {
        console.log("Got an error: " + obj)
        res.send(500)
      } else {
        controller.show(req,res)
      }
      
   });
});

//create for model
app.get('/:model/create', function(req,res){
   var controller = require('../app/controllers/' + req.params.model)
   controller.create(req,res)
});

//update for model
app.put('/:model/update/:id/:vid', function(req,res){
   var controller = require('../app/controllers/' + req.params.model)
   controller.load(req,res,req.params.id,req.params.vid, function(obj) {
     if(obj.constructor.name=="Error")
     {
       console.log("Got an error: " + obj)
       res.send(500)
     } else {
       controller.update(req,res)
     }
   });
});

//destroy for model
app.delete('/:model/destroy/:id/:vid', function(req,res){
  var controller = require('../app/controllers/' + req.params.model)
  controller.load(req,res,req.params.id,req.params.vid, function(obj) {
    if(obj.constructor.name=="Error")
    {
      console.log("Got an error: " + obj)
      res.send(500)
    } else {
      controller.destroy(req,res)
    }
  });
  
});

exports.app = app;