PLUGIN = test/files/test_plugin

update:
	lein run ${PLUGIN}
	/bin/cp -pf ${PLUGIN}/doc/foo.txt ${PLUGIN}/expected_helpfile.txt
	/bin/rm -rf ${PLUGIN}/doc
