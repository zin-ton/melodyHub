/*
This Lambda function is triggered when a user uploads a new video to the /video path
in S3 and generates a thumbnail for it.
Written by: Nazar Kuziv — 11.05.2025
*/

const { S3Client, GetObjectCommand, PutObjectCommand } = require('@aws-sdk/client-s3');
const fs = require('fs');
const path = require('path');
const crypto = require('crypto');
const { spawn } = require('child_process');
const ffmpegPath = '/opt/nodejs/ffmpeg';

const s3 = new S3Client({ region: 'eu-north-1' });

exports.handler = async (event) => {
  const bucket = event.Records[0].s3.bucket.name;
  const key = decodeURIComponent(event.Records[0].s3.object.key.replace(/\+/g, ' '));

  if (!key.endsWith('.mp4')) {
    console.log('Not an MP4 file, skipping.');
    return;
  }

  console.log(`Processing file: ${key}`);

  const getObjectCommand = new GetObjectCommand({ Bucket: bucket, Key: key });
  const { Body } = await s3.send(getObjectCommand);

  const tmpVideoPath = `/tmp/input-${crypto.randomUUID()}.mp4`;
  const writeStream = fs.createWriteStream(tmpVideoPath);
  await new Promise((resolve, reject) => {
    Body.pipe(writeStream)
      .on('finish', resolve)
      .on('error', reject);
  });

  console.log('Video downloaded to:', tmpVideoPath);

  const duration = await getVideoDuration(tmpVideoPath);
  const seekTime = duration ? Math.floor(duration * 0.25) : 1;
  console.log(`Video duration: ${duration}s, Seek time: ${seekTime}s`);

  const tmpImagePath = `/tmp/screenshot-${crypto.randomUUID()}.jpg`;

  await new Promise((resolve, reject) => {
    const ffmpeg = spawn(ffmpegPath, [
      '-ss', seekTime.toString(),
      '-i', tmpVideoPath,
      '-frames:v', '1',
      '-qscale:v', '2',
      '-y',
      tmpImagePath
    ]);

    ffmpeg.stderr.on('data', data => console.log(data.toString()));
    ffmpeg.on('close', (code) => code === 0 ? resolve() : reject(new Error(`ffmpeg exited with code ${code}`)));
  });

  console.log('Screenshot created:', tmpImagePath);

  const imageBuffer = fs.readFileSync(tmpImagePath);
  const imageKey = key.replace('videos/', 'images/').replace('.mp4', '.jpg');

  await s3.send(new PutObjectCommand({
    Bucket: bucket,
    Key: imageKey,
    Body: imageBuffer,
    ContentType: 'image/jpeg',
  }));

  console.log(`Thumbnail uploaded to: ${imageKey}`);

  fs.unlinkSync(tmpVideoPath);
  fs.unlinkSync(tmpImagePath);
};

async function getVideoDuration(filePath) {
  return new Promise((resolve, reject) => {
    const ffmpeg = spawn(ffmpegPath, ['-i', filePath]);

    let stderr = '';
    ffmpeg.stderr.on('data', data => stderr += data.toString());

    ffmpeg.on('close', () => {
      const match = stderr.match(/Duration:\s(\d+):(\d+):(\d+\.\d+)/);
      if (!match) return resolve(null);
      const [_, hours, minutes, seconds] = match;
      const duration = parseInt(hours) * 3600 + parseInt(minutes) * 60 + parseFloat(seconds);
      resolve(duration);
    });

    ffmpeg.on('error', reject);
  });
}