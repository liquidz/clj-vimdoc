""" Execute |foo#plus|
command! Foo call foo#plus(1, 2)

""" Execure |foo#bar|
""" ooo
command! -nargs=1 Bar call foo#bar(<q-args>)

""" Mapping for Foo
nnoremap <silent> <Plug>(foo_bar) :<C-u>Foo<CR>

