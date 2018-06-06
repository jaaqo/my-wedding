import '../sass/index.scss';

window.deps = {
  'jquery': require('jquery'),
  'popper': require('popper.js'),
  'react' : require('react'),
  'react-dom' : require('react-dom'),
  'react-recaptcha': require('react-recaptcha')
};

window.React = window.deps['react'];
window.ReactDOM = window.deps['react-dom'];
window.$ = window.jQuery = window.deps['jquery'];
window.Popper =  window.deps['popper'];
require('bootstrap');
