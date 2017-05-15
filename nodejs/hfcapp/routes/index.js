var express = require('express');
var router = express.Router();

/* GET home page. */
router.get('/', function(req, res, next) {
  res.render('index', { title: 'Express' });
});

router.get('/home', function(req, res, next) {
	  res.render('home', { title: 'Express' });
	});

router.get('/opticalTran', function(req, res, next) {
	  res.render('opticalTran', { title: 'Express' });
	});

module.exports = router;
