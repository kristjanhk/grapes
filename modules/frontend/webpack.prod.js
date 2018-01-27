const Merge = require('webpack-merge');
const CommonConfig = require('./webpack.common');

module.exports = new Merge(CommonConfig, {
  output: {
    filename: '[name].[hash].js',
    publicPath: '/static/dist/'
  },
  plugins: [

  ]
});
