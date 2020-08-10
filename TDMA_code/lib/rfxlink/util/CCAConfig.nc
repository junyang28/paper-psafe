interface CCAConfig
{
	
	command error_t setCCA(uint8_t cca);
	command uint8_t getCCA();
}
