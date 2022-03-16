import 'package:dio/dio.dart';
import 'package:equatable/equatable.dart';
import 'package:http_parser/http_parser.dart';
import 'package:kyc/middleware/models/merapi.dart';

class PostKycUploadRequest extends Equatable implements MerapiInputData {
  const PostKycUploadRequest({required this.docType, required this.filePath});

  /// Document type, as string.
  final String docType;

  /// Full path to the filename
  final String filePath;

  @override
  Future<Map<String, dynamic>> toMap() async {
    final contentType = MediaType('image', 'jpeg');

    return <String, dynamic>{
      'type': docType,
      'auto_upload_file': await MultipartFile.fromFile(
        filePath,
        contentType: contentType,
        filename: '$docType.jpg',
      )
    };
  }

  @override
  List<Object> get props => [docType, filePath];
}
