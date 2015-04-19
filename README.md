# clj-vimdoc [![Circle CI](https://circleci.com/gh/liquidz/clj-vimdoc.svg?style=svg)](https://circleci.com/gh/liquidz/clj-vimdoc)

Yet another vim helpfile generation tool.

**clj-vimdoc is ALPHA version.**

## Install

```
git clone https://github.com/liquidz/clj-vimdoc
cd clj-vimdoc
./configure
ln -s bin/vimdoc /path/to/your/bin
```

## Usage

 * generate vimdoc.yml template
```
cd /path/to/vim/plugin
vimdoc init
```
 * generate helpfile
```
cd /path/to/vim/plugin
vimdoc .
```

## License

Copyright (c) 2015 [uochan](http://twitter.com/uochan)

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
