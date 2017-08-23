var express = require('express');
var router = express.Router();

/* GET home page. */
router.get('/', function(req, res, next) {
  res.render('index', { title: 'Express' });
});

router.get('/login', function(req, res, next) {
	  res.render('login', { title: 'Express' });
	});

router.get('/home', function(req, res, next) {
	  res.render('home', { title: 'Express' });
	});

router.get('/opticalTran', function(req, res, next) {
	  res.render('opticalTran', { title: 'Express' });
	});

router.get('/rece_workstation', function(req, res, next) {
	  res.render('rece_workstation', { title: 'Express' });
	});

router.get('/edfa', function(req, res, next) {
	  res.render('edfa', { title: 'Express' });
	});
router.get('/emtrans', function(req, res, next) {
  res.render('emtrans', { title: 'Express' });
});

router.get('/osw', function(req, res, next) {
  res.render('osw', { title: 'Express' });
});


module.exports = router;
