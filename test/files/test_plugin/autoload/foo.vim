""
" hello
" world
"

""
" @var
" enabling flag
if !exists('g:foo#enale')
  let g:foo#enable = 0
endif

""
" Return (x + y)
" this is test function
" Example: >
"   foo#plus(10, 20)
" <
function! foo#plus(x, y) abort
  return a:x + a:y
endfunction
