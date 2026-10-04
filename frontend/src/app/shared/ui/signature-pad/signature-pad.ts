import { AfterViewInit, Component, ElementRef, signal, viewChild } from '@angular/core';

@Component({
  selector: 'app-signature-pad',
  imports: [],
  templateUrl: './signature-pad.html',
  styleUrl: './signature-pad.scss',
})
export class SignaturePad implements AfterViewInit {
  private readonly canvasRef = viewChild.required<ElementRef<HTMLCanvasElement>>('canvas');
  private ctx: CanvasRenderingContext2D | null = null;
  private drawing = false;

  readonly hasSignature = signal(false);

  ngAfterViewInit(): void {
    const canvas = this.canvasRef().nativeElement;
    const rect = canvas.getBoundingClientRect();
    canvas.width = rect.width;
    canvas.height = rect.height;

    this.ctx = canvas.getContext('2d');
    if (this.ctx) {
      this.ctx.lineWidth = 2;
      this.ctx.lineCap = 'round';
      this.ctx.strokeStyle = '#1A1F1C';
    }
  }

  onPointerDown(event: PointerEvent): void {
    if (!this.ctx) return;
    this.drawing = true;
    this.hasSignature.set(true);
    const { x, y } = this.relativeCoords(event);
    this.ctx.beginPath();
    this.ctx.moveTo(x, y);
  }

  onPointerMove(event: PointerEvent): void {
    if (!this.drawing || !this.ctx) return;
    const { x, y } = this.relativeCoords(event);
    this.ctx.lineTo(x, y);
    this.ctx.stroke();
  }

  onPointerUp(): void {
    this.drawing = false;
  }

  clear(): void {
    const canvas = this.canvasRef().nativeElement;
    this.ctx?.clearRect(0, 0, canvas.width, canvas.height);
    this.hasSignature.set(false);
  }

  toBlob(): Promise<Blob | null> {
    const canvas = this.canvasRef().nativeElement;
    return new Promise((resolve) => canvas.toBlob((blob) => resolve(blob), 'image/png'));
  }

  private relativeCoords(event: PointerEvent): { x: number; y: number } {
    const canvas = this.canvasRef().nativeElement;
    const rect = canvas.getBoundingClientRect();
    return { x: event.clientX - rect.left, y: event.clientY - rect.top };
  }
}
