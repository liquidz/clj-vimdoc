"" hello
"" world

""" enabling flag
let g:foo#enable = get(g:, 'foo#enable', 0)

""" Return (x + y)
""" this is test function
""" Example: >
"""   foo#plus(10, 20)
""" <
function! foo#plus(x, y) abort
  return a:x + a:y
endfunction
