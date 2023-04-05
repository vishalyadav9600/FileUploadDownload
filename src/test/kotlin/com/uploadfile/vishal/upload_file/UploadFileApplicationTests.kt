package com.uploadfile.vishal.upload_file

import com.uploadfile.vishal.upload_file.Exception.UnsupportedFormat
import com.uploadfile.vishal.upload_file.Service.FileUploadDownloadService
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.core.io.Resource
import org.springframework.http.ResponseEntity
import org.springframework.http.codec.multipart.FilePart
import org.springframework.util.unit.DataSize
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.io.FileNotFoundException
import java.nio.file.Path
import javax.naming.SizeLimitExceededException


//it provides support for creating and managing mock object
@ExtendWith(MockitoExtension::class)
class InjectTextResourcesTests() {
	@InjectMocks
	private lateinit var service: FileUploadDownloadService;
	@Test
	fun testFile() {
		//fxn create filepart object and configure to return filename
		val filePart: FilePart = mock(FilePart::class.java)
		Mockito.`when`(filePart.filename()).thenReturn("TestImage.png")
		Mockito.`when`(filePart.transferTo(Mockito.any(Path::class.java))).thenReturn(Mono.empty())
		var response:Mono<ResponseEntity<String>> = service.uploadFile(
			Flux.just(filePart),
			DataSize.ofKilobytes(5000).toBytes()).doOnNext {
			p -> println(p.body)
		}
		//creates a step verifier instance with response mono object
		StepVerifier.create(response)
			.expectNextMatches{obj -> obj.statusCode.is2xxSuccessful}
			.verifyComplete()
	}
	@Test
	fun fileLimitExceeded(){
		val filePart: FilePart = mock(FilePart::class.java)
		var response:Mono<ResponseEntity<String>> = service.uploadFile(Flux.just(filePart),
			DataSize.ofKilobytes(100000).toBytes())
		StepVerifier.create(response)
			.verifyError(SizeLimitExceededException::class.java)
	}
	@Test
	fun testFileNotFound() {

		val filePart: FilePart = mock(FilePart::class.java)
		Mockito.`when`(filePart.filename()).thenReturn("image.png")
		var response: Mono<ResponseEntity<Resource>> = service.downloadFile(filePart.filename())
		StepVerifier.create(response)
			.expectErrorMatches{ it is FileNotFoundException }
			.verify()
	}
	@Test
	fun testFileFound() {

		val filePart: FilePart = mock(FilePart::class.java)
		Mockito.`when`(filePart.filename()).thenReturn("images.png")
		var response: Mono<ResponseEntity<Resource>> = service.downloadFile(filePart.filename())
		StepVerifier.create(response)
			.expectNextMatches{obj->obj.statusCode.is2xxSuccessful}
			.verifyComplete()
	}
	@Test
	fun testDownloadFile() {
			val filePart: FilePart = mock(FilePart::class.java)
			Mockito.`when`(filePart.filename()).thenReturn("TestImage.zip")
			var response:Mono<ResponseEntity<String>> = service.uploadFile(Flux.just(filePart), DataSize.ofKilobytes(5000).toBytes()).doOnNext {
					p -> println(p.body)
			}
			StepVerifier.create(response)
				.verifyError(UnsupportedFormat::class.java)
	}
}