let gulp                     = require('gulp');
let gulpif                   = require('gulp-if');
let sass                     = require('gulp-sass');
let cleanCSS                 = require('gulp-clean-css');
let htmlmin                  = require('gulp-html-minifier');
let uglify                   = require('gulp-uglify')
let rename                   = require("gulp-rename");
let babel                    = require('gulp-babel');
let runSequence              = require('run-sequence').use(gulp);
let del                      = require('del');

let config = {
    src: './src/app',
    dest: './dist',
    bin: './dist/bin',
    app: './dist/src/app'
};

gulp.task('clean:dist', function () {
    return del.sync(config.dest);
});

gulp.task('copy:src', ['clean:dist'], function () {
    return gulp.src([config.src + '/**/*'])
        .pipe(gulpif(/.+\.scss/g, sass({outputStyle: 'compressed'}).on('error', sass.logError)))
        .pipe(gulpif(/.+\.css/g, cleanCSS({compatibility: 'ie9'})))
        .pipe(gulpif(/.+\.html/g, htmlmin({
            collapseWhitespace: true,
            caseSensitive: true,
            removeComments: true,
            removeRedundantAttributes: true
        })))
        .pipe(rename(function (path) {
            if (path.extname === '.css') {
                path.extname = '.scss';
            }
        }))
        .pipe(gulp.dest(config.app));
});

gulp.task('copy:env', ['compress:js'], function () {
    return gulp.src([
        '.babelrc',
        '.eslintrc',
        'package.json', './bin/**/*', './src/assets/**/*'], {base: '.', read: true})
        .pipe(rename(function (path) {
            if (path.basename == 'package.w' && path.extname == '.json') {
                path.basename = 'package';
            }
        }))
        .pipe(gulp.dest(config.dest));
});

gulp.task('compress:js', ['copy:src'], function () {
    return gulp.src([config.dest + '/**/*.js'])
        .pipe(babel())
        .pipe(uglify())
        .pipe(gulp.dest(config.dest));
});

gulp.task('npm:gulp', function (callback) {
    runSequence(['clean:dist', 'copy:src', 'compress:js', 'copy:env'], callback);
});