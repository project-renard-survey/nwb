from doc_tests import *
from view_tests import *

class MagicTestCase(TestCase):
	def testHttp(self):
		from urllib2 import urlopen
		urlopen("http://google.com", None)
		urlopen("http://gooahgdhdfhjadfhjfahjadjajnternabagle.com", None)
		self.assertFalse(True)
