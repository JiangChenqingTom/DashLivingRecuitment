export interface Message {
  id: number;
  content: string;
  username: string;
  parentId: number;
  createTime: Date; 
  children: Message[];
}